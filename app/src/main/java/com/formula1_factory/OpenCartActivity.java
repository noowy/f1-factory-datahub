package com.formula1_factory;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpenCartActivity extends AppCompatActivity
{
	private ListView productsListView;
	private EditText totalPriceField;
	private EditText availabilityDateField;
	private Button orderProductsButton;
	private ProgressBar loadingCircle;
	private RelativeLayout mainWindow;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> cartItems;
	private String clientID;
	private Boolean isAvailableNow = true;
	private String completionTime;
	private String totalPrice;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_cart);

		productsListView = (ListView) findViewById(R.id.products_list);
		totalPriceField = (EditText) findViewById(R.id.total_price_field);
		availabilityDateField = (EditText) findViewById(R.id.availability_date_field);
		orderProductsButton = (Button) findViewById(R.id.order_button);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		mainWindow = (RelativeLayout) findViewById(R.id.main_window);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		cartItems = (ArrayList<HashMap<String, String>>)
				sourceIntent.getSerializableExtra("cartItems");
		clientID = sourceIntent.getStringExtra("clientID");
		availabilityCheck();
		new LoadOrderInfo().execute();

		orderProductsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AsyncTask<Void, Void, Boolean> processOrder = new ProcessMarketOrder().execute();
				try
				{
					// if order isn't processed then a toast is also made
					if (!processOrder.get(20, TimeUnit.SECONDS))
					{
						Toast.makeText(OpenCartActivity.this,
								R.string.server_unavailable,
								Toast.LENGTH_LONG).show();
						return;
					}
				}
				catch (ExecutionException | InterruptedException e)
				{
					e.printStackTrace();
					return;
				}
				catch (TimeoutException e)
				{
					e.printStackTrace();
					Toast.makeText(OpenCartActivity.this,
							R.string.timeout_error,
							Toast.LENGTH_LONG).show();
					return;
				}

				Intent resultIntent = getIntent();
				setResult(MainActivity.ORDERED, resultIntent);

				finish();
			}
		});
	}

	private void availabilityCheck()
	{
		for (HashMap<String, String> product : cartItems)
		{
			if (Integer.parseInt(product.get("qtyToBuy")) > Integer.parseInt(product.get("qtyInStock")))
				isAvailableNow = false;
		}
	}

	class LoadOrderInfo extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{

			try
			{
				if (!isAvailableNow)
				{
					completionTime = dataManager.getDataFromDB(
							"SELECT " +
									"(SUM(Route_Process.transportation_time) + " +
									"SUM(Process.completion_time)) " +
									"AS completion_time " +
									"FROM Route_Process " +
									"JOIN Route " +
									"ON Route_Process.route_id=Route.ID " +
									"JOIN Process " +
									"ON Route_Process.process_id=Process.ID " +
									"WHERE Route.ID IN " +
									"(SELECT DISTINCT route_id " +
									"FROM Specification " +
									"WHERE product_id IN " + productIDsToString() + ");").
							getJSONObject(0).optString("completion_time");
					if (completionTime == null)
						return false;
				}

				totalPrice = dataManager.getDataFromDB(
						"SELECT SUM(price) AS total_price " +
								"FROM Detail " +
								"WHERE ID IN " + productIDsToString() + ";").
						getJSONObject(0).optString("total_price");

				if (totalPrice == null)
					return false;

				return true;

			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			loadingCircle.setVisibility(View.GONE);

			if (!result)
			{
				Toast.makeText(OpenCartActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_SHORT).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(OpenCartActivity.this,
					cartItems,
					R.layout.market_list_item,
					new String[] { "name", "qtyToBuy" },
					new int[] { R.id.product_name, R.id.product_quantity });
			productsListView.setAdapter(adapter);

			totalPriceField.setText(totalPrice);

			if (isAvailableNow)
				availabilityDateField.setText(R.string.today);
			else
			{
				availabilityDateField.setText(new Date(System.currentTimeMillis() +
						Integer.parseInt(completionTime) * 60 * 1000).toString()); // multiplication is for convertion to millis
			}
		}
	}

	// produces a string in form of (xxxx, ..., yyyy) for range sql statement
	private String productIDsToString()
	{
		StringBuilder range = new StringBuilder();
		range.append("(");
		for (int i = 0; i < cartItems.size(); i++)
		{
			if (i == 0)
			{
				range.append(cartItems.get(i).get("ID"));
				continue;
			}
			range.append(", " + cartItems.get(i).get("ID"));
		}
		range.append(")");

		return range.toString();
	}

	class ProcessMarketOrder extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			// this tints the main window and adds loading bar
			mainWindow.setForeground(new ColorDrawable(Color.parseColor("#82000000")));
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			Boolean result = false;

			try
			{
				HashMap<String, String> params = new HashMap<>();
				params.put("extra", "get_id");

				String orderID = dataManager.saveDataToDB(
						"INSERT INTO MarketOrder (order_date, client_id) " +
									"VALUES (NOW(), " + clientID + ");",
								params).getJSONObject(0).optString("new_id");

				if (orderID == null)
					return false;


				for (HashMap<String, String> product : cartItems)
				{
					result = dataManager.saveDataToDB(
						"INSERT INTO Product_Order (component_id, " +
									"quantity, " +
									"order_id, " +
									"is_packed) " +
								"VALUES (" + product.get("ID") + ", " +
									product.get("qtyToBuy") + ", " +
									orderID + ", " +
								isAvailableNow + ");");
				}

				if (!isAvailableNow)
				{
					for (HashMap<String, String> product : cartItems)
					{
						String routeID = dataManager.getDataFromDB(
								"SELECT route_id " +
										"FROM Specification " +
										"WHERE product_id=" + product.get("ID") + ";").
											getJSONObject(0).optString("route_id");
						String factoryOrderID = dataManager.saveDataToDB(
								"CALL add_factory_order(" +
												product.get("ID") + ", " +
												routeID + ", " +
												product.get("qtyToBuy") + ");",
										params).getJSONObject(0).optString("new_id");
						result = dataManager.updateDataInDB(
								"UPDATE Factory_Orders " +
										"SET market_order=" + orderID + " " +
										"WHERE ID=" + factoryOrderID + ";");
					}
				}
				else
				{
					for (HashMap<String, String> product : cartItems)
					{
						Integer qtyLeftInStock = Integer.parseInt(product.get("qtyInStock")) -
										Integer.parseInt(product.get("qtyToBuy"));
						result = dataManager.updateDataInDB(
										"UPDATE Stock " +
												"SET quantity=" + qtyLeftInStock.toString() + " " +
												"WHERE component_id=" + product.get("ID") + " AND " +
												"manufacture_date=\'" +
													product.get("manufacture_date") + "\';");
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			// this untints the main window and removes loading bar
			mainWindow.setForeground(new ColorDrawable(Color.parseColor("#00000000")));
			loadingCircle.setVisibility(View.GONE);
		}
	}
}
