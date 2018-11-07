package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class FactoryOrdersActivity extends AppCompatActivity
{
	public static final int DELETED = 200;

	private	ListView factoryOrdersListView;
	private ProgressBar loadingCircle;
	private Toolbar appToolbar;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> factoryOrdersList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factory_orders);

		factoryOrdersListView = (ListView) findViewById(R.id.factory_orders_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		factoryOrdersList = new ArrayList<>();
		new LoadAllFactoryOrders().execute();

		factoryOrdersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String factoryOrderID = ((TextView) view.findViewById(R.id.factory_order_id)).
						getText().toString();
				String detailName = ((TextView) view.findViewById(R.id.component_name)).
						getText().toString();
				String quantity = ((TextView) view.findViewById(R.id.component_quantity)).
						getText().toString();

				Intent openFactoryOrderIntent = new Intent(getApplicationContext(),
						OpenFactoryOrderActivity.class);

				openFactoryOrderIntent.putExtra("factoryOrderID", factoryOrderID);
				openFactoryOrderIntent.putExtra("detailName", detailName);
				openFactoryOrderIntent.putExtra("quantity", quantity);

				startActivityForResult(openFactoryOrderIntent, DELETED);
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
				updateFactoryOrdersListView(factoryOrdersList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateFactoryOrdersListView(searchFactoryOrders(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateFactoryOrdersListView(searchFactoryOrders(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateFactoryOrdersListView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(FactoryOrdersActivity.this,
				updatedList,
				R.layout.factory_order_item,
				new String[] {"ID", "name", "quantity"},
				new int[] {R.id.factory_order_id,
						R.id.component_name,
						R.id.component_quantity});
		factoryOrdersListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchFactoryOrders(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> factoryOrder : factoryOrdersList)
		{
			if (factoryOrder.get("name").toLowerCase().contains(query.toLowerCase()) ||
				factoryOrder.get("ID").toLowerCase().contains(query.toLowerCase()))
				result.add(factoryOrder);
		}

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == DELETED)
		{
			Intent reloadIntent = getIntent();
			finish();
			startActivity(reloadIntent);
		}
	}

	class LoadAllFactoryOrders extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonFactoryOrders;
			try
			{
				jsonFactoryOrders = dataManager.getDataFromDB(
						"SELECT Factory_Orders.ID, name, quantity " +
								"FROM Detail JOIN Factory_Orders " +
								"ON Factory_Orders.component_id=Detail.ID " +
								"ORDER BY name;");

				for (int i = 0; i < jsonFactoryOrders.length(); i++)
					factoryOrdersList.add(dataManager.jsonToHashMap(
							jsonFactoryOrders.getJSONObject(i)));
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

			if (factoryOrdersList.size() == 0)
			{
				Toast.makeText(FactoryOrdersActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(FactoryOrdersActivity.this,
					factoryOrdersList,
					R.layout.factory_order_item,
					new String[] {"ID", "name", "quantity"},
					new int[] {R.id.factory_order_id,
							R.id.component_name,
							R.id.component_quantity});
			factoryOrdersListView.setAdapter(adapter);
		}
	}
}
