package com.formula1_factory;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

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

		cartItems = (ArrayList<HashMap<String, String>>)
				sourceIntent.getSerializableExtra("cartItems");

		orderProductsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent resultIntent = getIntent();
				setResult(MainActivity.ORDERED, resultIntent);
				// don't forget to add .get() call to check the integrity
				// (prolly must do the same with orders)
				finish();
			}
		});
	}

	private boolean isAvailableNow()
	{
		return true;
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
			try
			{
				return dataManager.saveDataToDB("");
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
			// this untints the main window and removes loading bar
			mainWindow.setForeground(new ColorDrawable(Color.parseColor("#00000000")));
			loadingCircle.setVisibility(View.GONE);

			if (!result)
			{
				Toast.makeText(OpenCartActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
}
