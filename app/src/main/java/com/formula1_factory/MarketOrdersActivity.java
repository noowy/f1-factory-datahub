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

public class MarketOrdersActivity extends AppCompatActivity
{
	public static final int DELETED = 200;
	public static final int NO_ACTION = 100;

	private ListView marketOrdersListView;
	private ProgressBar loadingCircle;
	private Toolbar appToolbar;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> marketOrdersList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_market_orders);

		marketOrdersListView = (ListView) findViewById(R.id.market_orders_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		marketOrdersList = new ArrayList<>();
		new LoadAllMarketOrders().execute();

		marketOrdersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String orderID = ((TextView) view.findViewById(R.id.order_id)).
						getText().toString();
				Intent openOrderIntent = new Intent(getApplicationContext(),
						OpenMarketOrderActivity.class);
				openOrderIntent.putExtra("orderID", orderID);
				startActivityForResult(openOrderIntent, DELETED);
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
				updateMarketOrdersListView(marketOrdersList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateMarketOrdersListView(searchMarketOrder(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateMarketOrdersListView(searchMarketOrder(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateMarketOrdersListView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(MarketOrdersActivity.this,
				updatedList,
				R.layout.market_order_list_item,
				new String[] { "ID", "name", "order_date" },
				new int[] { R.id.order_id, R.id.client_name, R.id.order_date});
		marketOrdersListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchMarketOrder(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> marketOrder : marketOrdersList)
		{
			if (marketOrder.get("name").toLowerCase().contains(query.toLowerCase()) ||
					marketOrder.get("ID").toLowerCase().contains(query.toLowerCase()) ||
					marketOrder.get("order_date").toLowerCase().contains(query.toLowerCase()))
				result.add(marketOrder);
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


	private class LoadAllMarketOrders extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonMarketOrders;

			try
			{
				jsonMarketOrders = dataManager.getDataFromDB(
						"SELECT MarketOrder.ID, name, order_date " +
								"FROM MarketOrder JOIN Client " +
								"ON MarketOrder.client_id=Client.ID " +
								"ORDER BY name;");

				for (int i = 0; i < jsonMarketOrders.length(); i++)
					marketOrdersList.add(dataManager.jsonToHashMap(
							jsonMarketOrders.getJSONObject(i)));

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

			if (marketOrdersList.size() == 0)
			{
				Toast.makeText(MarketOrdersActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();

				return;
			}

			ListAdapter adapter = new SimpleAdapter(MarketOrdersActivity.this,
					marketOrdersList,
					R.layout.market_order_list_item,
					new String[] { "ID", "name", "order_date" },
					new int[] { R.id.order_id, R.id.client_name, R.id.order_date});
			marketOrdersListView.setAdapter(adapter);
		}
	}
}

