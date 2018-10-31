package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OpenFactoryOrderActivity extends AppCompatActivity
{
	private EditText orderIDField;
	private EditText productNameField;
	private EditText orderDateField;
	private EditText dueDateField;
	private EditText quantityField;
	private EditText routeNameField;
	private Button deleteFactoryOrderButton;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private String factoryOrderID;
	private HashMap<String, String> factoryOrderInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_factory_order);

		orderIDField = (EditText) findViewById(R.id.component_id_field);
		productNameField = (EditText) findViewById(R.id.product_name_field);
		orderDateField = (EditText) findViewById(R.id.order_date_field);
		dueDateField = (EditText) findViewById(R.id.due_date_field);
		quantityField = (EditText) findViewById(R.id.quantity_field);
		routeNameField = (EditText) findViewById(R.id.route_field);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		deleteFactoryOrderButton = (Button) findViewById(R.id.delete_factory_order_button);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		factoryOrderInfo = new HashMap<>();
		factoryOrderID = sourceIntent.getStringExtra("factoryOrderID");
		factoryOrderInfo.put("detailName", sourceIntent.getStringExtra("detailName"));
		factoryOrderInfo.put("quantity", sourceIntent.getStringExtra("quantity"));
		new LoadFactoryOrder().execute();

		deleteFactoryOrderButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new DeleteFactoryOrder().execute();
			}
		});
	}

	class LoadFactoryOrder extends AsyncTask<Void, Void, Void>
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

			try
			{
				jsonOrderInfo = dataManager.getDataFromDB(
						"SELECT Route.name, end_time, start_time " +
								"FROM Factory_Orders JOIN Route " +
								"ON Factory_Orders.route_id=Route.ID " +
								"WHERE Factory_Orders.ID=" + factoryOrderID + ";");

				factoryOrderInfo.putAll(dataManager.jsonToHashMap(
						jsonOrderInfo.getJSONObject(0)));
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
			orderIDField.setText(factoryOrderID);
			orderDateField.setText(factoryOrderInfo.get("start_time"));
			productNameField.setText(factoryOrderInfo.get("detailName"));
			dueDateField.setText(factoryOrderInfo.get("end_time"));
			quantityField.setText(factoryOrderInfo.get("quantity"));
			routeNameField.setText(factoryOrderInfo.get("name"));

			try
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if (formatter.parse(factoryOrderInfo.get("start_time")).after(
						new Date(System.currentTimeMillis() - 86400000))) // 86400000 represents one day in milliseconds
					deleteFactoryOrderButton.setVisibility(View.VISIBLE);
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

	private class DeleteFactoryOrder extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			try
			{
				return dataManager.deleteDataFromDB(
						"DELETE FROM Factory_Orders " +
								"WHERE Factory_Orders.ID=" + factoryOrderID + ";");
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
				setResult(FactoryOrdersActivity.DELETED, responseIntent);
				finish();
			}
			else
			{
				Toast.makeText(OpenFactoryOrderActivity.this,
						R.string.error_occurred,
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
