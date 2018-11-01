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

import java.util.HashMap;

public class OpenProductActivity extends AppCompatActivity
{
	private EditText productIDField;
	private EditText productNameField;
	private EditText quantityField;
	private EditText descriptionField;
	private EditText lifespanField;
	private EditText manufactureDateField;
	private EditText expirationDateField;
	private TextView manufactureDateTextView;
	private TextView expirationDateTextView;
	private ProgressBar loadingCircle;
	private Button addToCartButton;
	private Button addQuantityButton;
	private Button subtractQuantitybutton;
	private TextView quantityToBuyTextView;

	private DataManager dataManager;

	private HashMap<String, String> productInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_product);

		productIDField = (EditText) findViewById(R.id.prodcut_id_field);
		productNameField = (EditText) findViewById(R.id.product_name_field);
		quantityField = (EditText) findViewById(R.id.quantity_field);
		descriptionField = (EditText) findViewById(R.id.description_field);
		lifespanField = (EditText) findViewById(R.id.lifespan_field);
		manufactureDateField =(EditText) findViewById(R.id.manufacture_date_field);
		expirationDateField = (EditText) findViewById(R.id.expiration_date_field);
		manufactureDateTextView = (TextView) findViewById(R.id.manufacture_date_textview);
		expirationDateTextView = (TextView) findViewById(R.id.expiration_date_textview);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		addToCartButton = (Button) findViewById(R.id.add_to_cart_button);
		addQuantityButton = (Button) findViewById(R.id.add_quantity_button);
		subtractQuantitybutton = (Button) findViewById(R.id.subtract_quantity_button);
		quantityToBuyTextView = (TextView) findViewById(R.id.quantity_to_buy_textview);

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		productInfo = new HashMap<>();

		productInfo.put("ID", sourceIntent.getStringExtra("ID"));
		productInfo.put("name", sourceIntent.getStringExtra("name"));
		productInfo.put("quantity", sourceIntent.getStringExtra("quantity"));
		new LoadProduct().execute();

		addToCartButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent resultIntent = getIntent();
				setResult(MainActivity.ADD_TO_CART, resultIntent);
				resultIntent.putExtra("ID", productInfo.get("ID"));
				resultIntent.putExtra("quantity", quantityToBuyTextView.getText().toString());
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

	class LoadProduct extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			JSONArray jsonProductInfo;

			try
			{
				// if quantity is null, then there is no such item in stock
				// hence there's no need to address Stock relation
				if (productInfo.get("quantity").equals("0"))
				{
					jsonProductInfo = dataManager.getDataFromDB(
							"SELECT description, lifespan " +
									"FROM Detail " +
									"WHERE Detail.ID=" + productInfo.get("ID") + ";");
				}
				else
				{
					jsonProductInfo = dataManager.getDataFromDB(
							"SELECT description, lifespan, manufacture_date, expiration_date " +
									"FROM Stock JOIN Detail " +
									"ON Stock.component_id=Detail.ID " +
									"WHERE Stock.component_id=" + productInfo.get("ID") + ";");
				}

				productInfo.putAll(dataManager.jsonToHashMap(
						jsonProductInfo.getJSONObject(0)));
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
				Toast.makeText(OpenProductActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_SHORT).show();
				return;
			}

			productIDField.setText(productInfo.get("ID"));
			productNameField.setText(productInfo.get("name"));
			quantityField.setText(productInfo.get("quantity") + "pcs.");
			descriptionField.setText(productInfo.get("description"));
			lifespanField.setText(productInfo.get("lifespan") + " days");

			if (!productInfo.get("quantity").equals("0"))
			{
				manufactureDateField.setText(productInfo.get("manufacture_date"));
				manufactureDateTextView.setVisibility(View.VISIBLE);
				manufactureDateField.setVisibility(View.VISIBLE);

				expirationDateField.setText(productInfo.get("expiration_date"));
				expirationDateTextView.setVisibility(View.VISIBLE);
				expirationDateField.setVisibility(View.VISIBLE);
			}
		}
	}
}
