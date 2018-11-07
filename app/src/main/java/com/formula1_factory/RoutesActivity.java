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

public class RoutesActivity extends AppCompatActivity
{
	private ListView routesListView;
	private ProgressBar loadingCircle;
	private Toolbar appToolbar;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> routesList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);

		routesListView = (ListView) findViewById(R.id.routes_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		routesList = new ArrayList<>();
		new LoadAllRoutes().execute();

		routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String name = ((TextView) view.findViewById(android.R.id.text1)).
						getText().toString();
				String ID = ((TextView) view.findViewById(android.R.id.text2)).
						getText().toString();

				Intent intent = new Intent(getApplicationContext(), OpenRouteActivity.class);

				intent.putExtra("routeName", name);
				intent.putExtra("routeID", ID);

				startActivity(intent);
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
				updateRoutesListView(routesList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateRoutesListView(searchRoutes(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateRoutesListView(searchRoutes(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateRoutesListView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(RoutesActivity.this,
				updatedList,
				android.R.layout.simple_list_item_2,
				new String[] { "name", "ID" },
				new int[] { android.R.id.text1, android.R.id.text2 });
		routesListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchRoutes(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> route : routesList)
		{
			if (route.get("name").toLowerCase().contains(query.toLowerCase()) ||
				route.get("ID").toLowerCase().contains(query.toLowerCase()))
				result.add(route);
		}

		return result;
	}

	private class LoadAllRoutes extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonRoutes;

			try
			{
				jsonRoutes = dataManager.getDataFromDB(
						"SELECT name, ID " +
								"FROM Route " +
								"ORDER BY name;");

				for (int i = 0; i < jsonRoutes.length(); i++)
					routesList.add(dataManager.jsonToHashMap(
							jsonRoutes.getJSONObject(i)));
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

			if (routesList.size() == 0)
			{
				Toast.makeText(RoutesActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(RoutesActivity.this,
					routesList,
					android.R.layout.simple_list_item_2,
					new String[] { "name", "ID" },
					new int[] { android.R.id.text1, android.R.id.text2 });
			routesListView.setAdapter(adapter);
		}
	}
}
