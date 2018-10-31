package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OpenMarketOrderActivity extends AppCompatActivity
{
	private EditText orderIDField;
	private EditText clientNameField;
	private EditText orderDateField;
	private ListView productsListView;
	private Button deleteOrderButton;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private String orderID;
	private HashMap<String, String> orderInfo;
	private ArrayList<HashMap<String, String>> productsList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_market_order);

		orderIDField = (EditText) findViewById(R.id.component_id_field);
		clientNameField = (EditText) findViewById(R.id.client_name_field);
		orderDateField = (EditText) findViewById(R.id.order_date_field);
		productsListView = (ListView) findViewById(R.id.product_names_listview);
		deleteOrderButton = (Button) findViewById(R.id.delete_market_order_button);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		productsList = new ArrayList<>();
		orderInfo = new HashMap<String, String>();
		orderID = sourceIntent.getStringExtra("orderID");
		new LoadMarketOrder().execute();


		deleteOrderButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new DeleteMarketOrder().execute();
			}
		});
	}

	class LoadMarketOrder extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonOrderInfo;
			JSONArray jsonOrderProducts;

			try
			{
				jsonOrderInfo = dataManager.getDataFromDB(
						"SELECT MarketOrder.ID, Client.name, order_date " +
								"FROM MarketOrder JOIN Client " +
								"ON MarketOrder.client_id=Client.ID " +
								"WHERE MarketOrder.ID=" + orderID + ";");
				jsonOrderProducts = dataManager.getDataFromDB(
						"SELECT Detail.name, quantity " +
								"FROM Detail JOIN Product_Order " +
								"ON Product_Order.component_id=Detail.ID " +
								"WHERE order_id=" + orderID + ";");

				orderInfo = dataManager.jsonToHashMap(jsonOrderInfo.getJSONObject(0));
				for (int i = 0; i < jsonOrderProducts.length(); i++)
					productsList.add(dataManager.jsonToHashMap(
							jsonOrderProducts.getJSONObject(i)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			orderIDField.setText(orderInfo.get("ID"));
			orderDateField.setText(orderInfo.get("order_date"));
			clientNameField.setText(orderInfo.get("name"));

			ListAdapter adapter = new SimpleAdapter(OpenMarketOrderActivity.this,
					productsList,
					android.R.layout.simple_list_item_2,
					new String[] { "name", "quantity", },
					new int[] { android.R.id.text1, android.R.id.text2 });
			productsListView.setAdapter(adapter);

			try
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //  mysql date time format
				if (formatter.parse(orderInfo.get("order_date")).after(
						new Date(System.currentTimeMillis() - 86400000))) // 86400000 represents one day in milliseconds
					deleteOrderButton.setVisibility(View.VISIBLE);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			finally
			{
				loadingCircle.setVisibility(View.GONE);
			}
		}
	}

	private class DeleteMarketOrder extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			try
			{
				return dataManager.deleteDataFromDB(
						"DELETE FROM MarketOrder " +
								"WHERE ID=" + orderID + ";");
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
			if (result)
			{
				Intent responseIntent = getIntent();
				setResult(MarketOrdersActivity.DELETED, responseIntent);
				finish();
			}
			else
			{
				Toast.makeText(OpenMarketOrderActivity.this,
						R.string.error_occurred,
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
