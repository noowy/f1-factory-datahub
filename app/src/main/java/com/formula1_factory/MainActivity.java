package com.formula1_factory;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.*;
import android.view.Menu;
import android.view.MenuInflater;
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
	private android.support.v7.widget.Toolbar appToolbar;

    private DataManager dataManager;

    private ArrayList<HashMap<String, String>> cartItems;
    private ArrayList<HashMap<String, String>> productsList;
    private ListAdapter productsAdapter;
    private String clientID;
    private boolean backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		manufactureButton = (Button) findViewById(R.id.manufacture_button);
		openCartButton = (Button) findViewById(R.id.open_cart_button);
		productsListView = (ListView) findViewById(R.id.products_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		productsList = new ArrayList<HashMap<String, String>>();
		cartItems = new ArrayList<>();

		new LoadProductsTask().execute();

		Intent sourceIntent = getIntent();
		clientID = sourceIntent.getStringExtra("clientID");
		clientID = clientID.equals("null") ? null : clientID;

		// clients are not allowed to see manufacture side of app
		// and workers are not allowed to "buy" products
		if (clientID != null)
			manufactureButton.setVisibility(View.GONE);
		else
			openCartButton.setVisibility(View.GONE);


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
				openCartIntent.putExtra("clientID", clientID);
				startActivityForResult(openCartIntent, ORDERED);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_action_bar, menu);

		final android.support.v7.widget.SearchView searchView =
				(android.support.v7.widget.SearchView) menu.findItem(R.id.app_bar_search).getActionView();

		searchView.setOnCloseListener(new SearchView.OnCloseListener()
		{
			@Override
			public boolean onClose()
			{
				searchView.onActionViewCollapsed();
				updateProductsTextView(productsList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateProductsTextView(searchProducts(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateProductsTextView(searchProducts(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateProductsTextView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(MainActivity.this,
				updatedList,
				R.layout.market_list_item,
				new String[] {"ID", "name", "quantity"},
				new int[] {R.id.product_id, R.id.product_name, R.id.product_quantity });
		productsListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchProducts(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> product : productsList)
		{
			if (product.get("name").toLowerCase().contains(query.toLowerCase()) ||
				product.get("ID").toLowerCase().contains(query.toLowerCase()))
				result.add(product);
		}

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data)
	{
		if (responseCode == ADD_TO_CART)
		{
			HashMap<String, String> cartItem = new HashMap<>();
			cartItem.put("ID", data.getStringExtra("ID"));
			cartItem.put("name", data.getStringExtra("name"));
			cartItem.put("qtyToBuy", data.getStringExtra("qtyToBuy"));
			cartItem.put("qtyInStock", data.getStringExtra("qtyInStock"));
			cartItem.put("manufacture_date", data.getStringExtra("manufacture_date"));
			cartItem.put("price", data.getStringExtra("price"));

			for (int i = 0; i < cartItems.size(); i++)
			{
				if (cartItems.get(i).get("ID").equals(cartItem.get("ID")) &&
					cartItems.get(i).get("manufacture_date").equals(cartItem.get("manufacture_date")))
				{
					Integer newQuantity = Integer.parseInt(cartItems.get(i).get("qtyToBuy")) +
							Integer.parseInt(cartItem.get("qtyToBuy"));
					cartItem.put("qtyToBuy", newQuantity.toString());
					cartItems.set(i, cartItem);
					return;
				}
			}

			cartItems.add(cartItem);
		}
		else if (responseCode == ORDERED)
		{
			Intent reloadIntent = getIntent();
			finish();
			startActivity(reloadIntent);
		}
	}

	@Override
	public void onBackPressed()
	{
		if (hasWindowFocus())
		{
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

			dialogBuilder.setTitle(R.string.confirm_exit);
			dialogBuilder.setMessage(R.string.sureness_check);

			dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			});

			dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});

			AlertDialog alert = dialogBuilder.create();
			alert.show();
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

			productsAdapter = new SimpleAdapter(MainActivity.this,
					productsList,
					R.layout.market_list_item,
					new String[] {"ID", "name", "quantity"},
					new int[] {R.id.product_id, R.id.product_name, R.id.product_quantity });
			productsListView.setAdapter(productsAdapter);
		}
	}
}
