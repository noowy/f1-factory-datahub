package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.HashMap;

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
		manufactureDateField =(EditText) findViewById(R.id.availability_date_field);
		expirationDateField = (EditText) findViewById(R.id.expiration_date_field);
		manufactureDateTextView = (TextView) findViewById(R.id.availability_date_textview);
		expirationDateTextView = (TextView) findViewById(R.id.expiration_date_textview);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		componentInfo = new HashMap<>();

		componentInfo.put("ID", sourceIntent.getStringExtra("ID"));
		componentInfo.put("name", sourceIntent.getStringExtra("name"));
		componentInfo.put("quantity", sourceIntent.getStringExtra("quantity"));
		componentInfo.put("units", sourceIntent.getStringExtra("units"));
		new LoadComponent().execute();
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
							"SELECT description, lifespan " +
									"FROM Detail " +
									"WHERE Detail.ID=" + componentInfo.get("ID") + ";");
				}
				else
				{
					jsonComponentInfo = dataManager.getDataFromDB(
							"SELECT description, lifespan, manufacture_date, expiration_date " +
									"FROM Stock JOIN Detail " +
									"ON Stock.component_id=Detail.ID " +
									"WHERE Stock.component_id=" + componentInfo.get("ID") + ";");
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
			}
		}
	}
}
