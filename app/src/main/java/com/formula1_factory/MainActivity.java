package com.formula1_factory;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
	public static final Integer ADD_TO_CART = 200;
	public static final Integer ORDERED = 300;

    private Button manufactureButton;
    private ListView productsListView;
    private Button openCartButton;
	private ProgressBar loadingCircle;

    private DataManager dataManager;

    private ArrayList<HashMap<String, String>> cartItems;
    private ArrayList<HashMap<String, String>> productsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		manufactureButton = (Button) findViewById(R.id.manufacture_button);
		openCartButton = (Button) findViewById(R.id.open_cart_button);
		productsListView = (ListView) findViewById(R.id.products_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		dataManager = new DataManager();
		productsList = new ArrayList<HashMap<String, String>>();
		cartItems = new ArrayList<>();
		new LoadProductsTask().execute();

        manufactureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), ManufactureActivity.class);
                startActivity(intent);
            }
        });

		productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String ID = ((TextView) view.findViewById(R.id.product_id)).
						getText().toString();
				String name = ((TextView) view.findViewById(R.id.product_name)).
						getText().toString();
				String quantity = ((TextView) view.findViewById(R.id.product_quantity)).
						getText().toString();

				Intent openProductIntent = new Intent(getApplicationContext(),
						OpenProductActivity.class);

				openProductIntent.putExtra("ID", ID);
				openProductIntent.putExtra("name", name);
				openProductIntent.putExtra("quantity", quantity);
				startActivityForResult(openProductIntent, ADD_TO_CART);
			}
		});

		openCartButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent openCartIntent = new Intent(getApplicationContext(),
						OpenCartActivity.class);

				openCartIntent.putExtra("cartItems", cartItems);
				startActivityForResult(openCartIntent, ORDERED);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data)
	{
		if (responseCode == ADD_TO_CART)
		{
			HashMap<String, String> cartItem = new HashMap<>();
			cartItem.put("ID", data.getStringExtra("ID"));
			cartItem.put("qtyToBuy", data.getStringExtra("qtyToBuy"));
			cartItem.put("qtyInStock", data.getStringExtra("qtyInStock"));
			cartItems.add(cartItem);
		}
		else if (responseCode == ORDERED)
		{
			Intent reloadIntent = getIntent();
			finish();
			startActivity(reloadIntent);
		}
	}

    class LoadProductsTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonProducts = new JSONArray();
			try
			{
				jsonProducts = dataManager.getDataFromDB(
						"SELECT Detail.ID, name, quantity " +
								"FROM Detail LEFT OUTER JOIN Stock " +
								"ON Detail.ID=Stock.component_id " +
								"WHERE for_sale=true " +
								"ORDER BY name;");

				for (int i = 0; i < jsonProducts.length(); i++)
					productsList.add(dataManager.jsonToHashMap(jsonProducts.getJSONObject(i)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void _void)
		{
			loadingCircle.setVisibility(View.GONE);

			if (productsList.size() == 0)
			{
				Toast.makeText(MainActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(MainActivity.this,
					productsList,
					R.layout.market_list_item,
					new String[] {"ID", "name", "quantity"},
					new int[] {R.id.product_id, R.id.product_name, R.id.product_quantity });
			productsListView.setAdapter(adapter);
		}
	}
}
