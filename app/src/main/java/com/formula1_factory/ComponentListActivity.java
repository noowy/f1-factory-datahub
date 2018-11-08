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
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class ComponentListActivity extends AppCompatActivity
{
	public static final Integer UPDATED = 200;

	private ListView componentListView;
	private ProgressBar loadingCircle;
	private Toolbar appToolbar;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> componentList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_component_list);

		componentListView = (ListView) findViewById(R.id.component_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		componentList = new ArrayList<HashMap<String, String>>();
		new LoadAllComponentsTask().execute();

		componentListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String ID = ((TextView) view.findViewById(R.id.component_id)).
						getText().toString();
				String name = ((TextView) view.findViewById(R.id.component_name)).
						getText().toString();
				String quantity = ((TextView) view.findViewById(R.id.component_quantity)).
						getText().toString();
				String units = ((TextView) view.findViewById(R.id.component_units)).
						getText().toString();
				String manufactureDate = ((TextView) view.findViewById(R.id.manufacture_date)).
						getText().toString();

				Intent openComponentIntent = new Intent(getApplicationContext(),
						OpenComponentActivity.class);

				openComponentIntent.putExtra("ID", ID);
				openComponentIntent.putExtra("name", name);
				openComponentIntent.putExtra("quantity", quantity);
				openComponentIntent.putExtra("units", units);
				openComponentIntent.putExtra("manufacture_date", manufactureDate);

				startActivityForResult(openComponentIntent, UPDATED);
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
				updateComponentsListView(componentList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateComponentsListView(searchComponents(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateComponentsListView(searchComponents(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateComponentsListView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(ComponentListActivity.this,
				updatedList,
				R.layout.component_list_item ,
				new String[] { "ID", "name", "quantity", "units", "manufacture_date" },
				new int[] { R.id.component_id,
						R.id.component_name,
						R.id.component_quantity,
						R.id.component_units,
						R.id.manufacture_date });
		componentListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchComponents(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> component : componentList)
		{
			if (component.get("name").toLowerCase().contains(query.toLowerCase()) ||
				component.get("ID").toLowerCase().contains(query.toLowerCase()))
				result.add(component);
		}

		return result;
	}

	@Override
	protected void onActivityResult(int resultCode, int requestCode, Intent data)
	{
		if (resultCode == UPDATED)
		{
			Intent reloadIntent = getIntent();
			finish();
			startActivity(reloadIntent);
		}
	}

	class LoadAllComponentsTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonComponents = new JSONArray();
			try
			{
				jsonComponents = dataManager.getDataFromDB(
						"SELECT Detail.ID, name, quantity, units, manufacture_date " +
								"FROM Detail LEFT OUTER JOIN Stock " +
								"ON Detail.ID=Stock.component_id " +
								"ORDER BY name;");
				for (int i = 0; i < jsonComponents.length(); i++)
					componentList.add(dataManager.jsonToHashMap(
							jsonComponents.getJSONObject(i)));
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

			if (componentList.size() == 0)
			{
				Toast.makeText(ComponentListActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(ComponentListActivity.this,
					componentList,
					R.layout.component_list_item ,
					new String[] { "ID", "name", "quantity", "units", "manufacture_date" },
					new int[] { R.id.component_id,
							R.id.component_name,
							R.id.component_quantity,
							R.id.component_units,
							R.id.manufacture_date});
			componentListView.setAdapter(adapter);
		}
	}
}
