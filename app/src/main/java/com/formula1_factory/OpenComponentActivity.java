package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpenComponentActivity extends AppCompatActivity
{
	private EditText componentIDField;
	private EditText componentNameField;
	private EditText quantityField;
	private EditText descriptionField;
	private EditText lifespanField;
	private EditText manufactureDateField;
  	private EditText expirationDateField;
  	private TextView manufactureDateTextView;
  	private TextView expirationDateTextView;
 	private ProgressBar loadingCircle;
 	private Button reproduceButton;
	private Button orderButton;
	private Button addQuantityButton;
	private Button subtractQuantitybutton;
	private TextView quantityToBuyTextView;

	private DataManager dataManager;

	private HashMap<String, String> componentInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_component);

		componentIDField = (EditText) findViewById(R.id.component_id_field);
		componentNameField = (EditText) findViewById(R.id.component_name_field);
		quantityField = (EditText) findViewById(R.id.quantity_field);
		descriptionField = (EditText) findViewById(R.id.description_field);
		lifespanField = (EditText) findViewById(R.id.lifespan_field);
		manufactureDateField =(EditText) findViewById(R.id.manufacture_date_field);
		expirationDateField = (EditText) findViewById(R.id.expiration_date_field);
		manufactureDateTextView = (TextView) findViewById(R.id.manufacture_date_textview);
		expirationDateTextView = (TextView) findViewById(R.id.expiration_date_textview);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		reproduceButton = (Button) findViewById(R.id.reproduce_button);
		orderButton = (Button) findViewById(R.id.order_button);
		addQuantityButton = (Button) findViewById(R.id.add_quantity_button);
		subtractQuantitybutton = (Button) findViewById(R.id.subtract_quantity_button);
		quantityToBuyTextView = (TextView) findViewById(R.id.quantity_to_buy_textview);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		componentInfo = new HashMap<>();

		componentInfo.put("ID", sourceIntent.getStringExtra("ID"));
		componentInfo.put("name", sourceIntent.getStringExtra("name"));
		componentInfo.put("quantity", sourceIntent.getStringExtra("quantity"));
		componentInfo.put("units", sourceIntent.getStringExtra("units"));
		componentInfo.put("manufacture_date", sourceIntent.getStringExtra("manufacture_date"));
		new LoadComponent().execute();

		reproduceButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AsyncTask<Void, Void, Boolean> task = new ProcessReproducing().execute();
				
				try
				{
					if (!task.get(20, TimeUnit.SECONDS))
					{
						Toast.makeText(OpenComponentActivity.this,
								R.string.server_unavailable,
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				catch (TimeoutException e)
				{
					e.printStackTrace();
					Toast.makeText(OpenComponentActivity.this,
							R.string.timeout_error,
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent resultIntent = getIntent();
				setResult(ComponentListActivity.UPDATED, resultIntent);
				finish();
			}
		});

		orderButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Integer orderQuantity = Integer.parseInt(
						quantityToBuyTextView.getText().toString());
				AsyncTask<Integer, Void, Boolean> task = new ProcessOrdering().execute(orderQuantity);
				
				try
				{
					if (!task.get(20, TimeUnit.SECONDS))
					{
						Toast.makeText(OpenComponentActivity.this,
								R.string.server_unavailable,
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				catch (TimeoutException e)
				{
					e.printStackTrace();
					Toast.makeText(OpenComponentActivity.this,
							R.string.timeout_error,
							Toast.LENGTH_SHORT).show();
					return;
				}

				Intent resultIntent = getIntent();
				setResult(ComponentListActivity.UPDATED, resultIntent);
				finish();
			}
		});

		addQuantityButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Integer currentQuantity = Integer.parseInt(
						quantityToBuyTextView.getText().toString());
				currentQuantity++;
				quantityToBuyTextView.setText(currentQuantity.toString());
			}
		});

		subtractQuantitybutton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Integer currentQuantity = Integer.parseInt(
						quantityToBuyTextView.getText().toString());

				// check for quantity limit if subtract is to happen
				// i.e. you can not buy less than 1 item
				if (currentQuantity - 1 < 1)
					return;

				currentQuantity--;
				quantityToBuyTextView.setText(currentQuantity.toString());
			}
		});
	}

	class LoadComponent extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			JSONArray jsonComponentInfo;

			try
			{
				// if quantity is null, then there is no such item in stock
				// hence there's no need to address Stock relation
				if (componentInfo.get("quantity").equals("0"))
				{
					jsonComponentInfo = dataManager.getDataFromDB(
							"SELECT description, lifespan, price " +
									"FROM Detail " +
									"WHERE Detail.ID=" + componentInfo.get("ID") + ";");
					if (jsonComponentInfo == null)
						return false;
				}
				else
				{
					jsonComponentInfo = dataManager.getDataFromDB(
							"SELECT description, " +
										"lifespan, " +
										"manufacture_date, " +
										"expiration_date, " +
										"price " +
									"FROM Stock JOIN Detail " +
									"ON Stock.component_id=Detail.ID " +
									"WHERE Stock.component_id=" + componentInfo.get("ID") + " " +
									"AND manufacture_date=\"" +
										componentInfo.get("manufacture_date") + "\";");
					if (jsonComponentInfo == null)
						return false;
				}

				componentInfo.putAll(dataManager.jsonToHashMap(
						jsonComponentInfo.getJSONObject(0)));

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
				Toast.makeText(OpenComponentActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_SHORT).show();
				return;
			}

			componentIDField.setText(componentInfo.get("ID"));
			componentNameField.setText(componentInfo.get("name"));
			quantityField.setText(componentInfo.get("quantity") + componentInfo.get("units"));
			descriptionField.setText(componentInfo.get("description"));
			lifespanField.setText(componentInfo.get("lifespan") + " days");

			if (!componentInfo.get("quantity").equals("0"))
			{
				manufactureDateField.setText(componentInfo.get("manufacture_date"));
				manufactureDateTextView.setVisibility(View.VISIBLE);
				manufactureDateField.setVisibility(View.VISIBLE);

				expirationDateField.setText(componentInfo.get("expiration_date"));
				expirationDateTextView.setVisibility(View.VISIBLE);
				expirationDateField.setVisibility(View.VISIBLE);

				//  mysql date time format
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try
				{
					// reproducing is allowed only for expired components
					// restriction for price is cause bought components could not be reproduced
					// (negative price means outsource component)
					if (formatter.parse(componentInfo.get("expiration_date")).before(
							new Date(System.currentTimeMillis()))
							&&
							Integer.parseInt(componentInfo.get("price")) > 0)
						reproduceButton.setVisibility(View.VISIBLE);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
				catch (NumberFormatException e) // means the price isn't set (=null)
				{
					e.printStackTrace();
					reproduceButton.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	class ProcessReproducing extends AsyncTask<Void, Void, Boolean>
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
				JSONArray jsonRoute = dataManager.getDataFromDB(
						"SELECT DISTINCT route_id " +
								"FROM Specification " +
								"WHERE product_id=" + componentInfo.get("ID") + ";");

				return dataManager.deleteDataFromDB(
						"DELETE FROM Stock " +
								"WHERE component_id=" + componentInfo.get("ID") + " " +
								"AND manufacture_date=\"" +
									componentInfo.get("manufacture_date") + "\";")
						&&
						dataManager.updateDataInDB(
								"CALL add_factory_order(" +
										componentInfo.get("ID") + ", " +
										jsonRoute.getJSONObject(0).
												optString("route_id") + ", " +
										componentInfo.get("quantity") + ");")
						&&
						dataManager.saveDataToDB(
								"INSERT INTO Waste " +
											"(component_id, " +
											"quantity, " +
											"manufacture_date, " +
											"expiration_date" + ")" +
										"VALUES (" +
											componentInfo.get("ID") + ", " +
											componentInfo.get("quantity") + ", \"" +
											componentInfo.get("manufacture_date") + "\", \"" +
											componentInfo.get("expiration_date") + "\");");
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
		}
	}
	
	class ProcessOrdering extends AsyncTask<Integer, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(Integer... params)
		{
			int price;
			boolean isPurchased = true;
					
			try
			{
				price = Integer.parseInt(componentInfo.get("price"));

				if (price > 0)
					isPurchased = false;
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				isPurchased = false;
			}
			
			try
			{
				if (!isPurchased)
				{
					JSONArray jsonRoute = dataManager.getDataFromDB(
							"SELECT DISTINCT route_id " +
									"FROM Specification " +
									"WHERE product_id=" + componentInfo.get("ID") + ";");

					return dataManager.updateDataInDB(
							"CALL add_factory_order(" +
									componentInfo.get("ID") + ", " +
									jsonRoute.getJSONObject(0).
											optString("route_id") + ", " +
									params[0].toString() + ");");
				}
				else
				{
					// TODO: In this case one must get dates from outsource factory
					// this is all just to get a random expiration date
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					StringBuffer stringDate = new StringBuffer();
					Date date = new Date(System.currentTimeMillis() +
							9999999999L +
							new Random().nextInt(200000000));
					formatter.format(date, stringDate, new FieldPosition(0));

					return dataManager.saveDataToDB(
							"INSERT INTO Stock (" +
										"component_id, " +
										"quantity, " +
										"manufacture_date, " +
										"expiration_date) " +
									"VALUES (" +
										componentInfo.get("ID") + ", " +
										params[0].toString() + ", " +
										"NOW(), \"" +
										stringDate + "\");");
				}
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
		}
	}
}
