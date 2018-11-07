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

public class WorkstationListActivity extends AppCompatActivity
{
	private ListView workstationsListView;
	private ProgressBar loadingCircle;
	private Toolbar appToolbar;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> workstationsList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workstation_list);

		workstationsListView = (ListView) findViewById(R.id.workstations_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);
		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		dataManager = new DataManager();
		workstationsList = new ArrayList<>();
		new LoadAllWorkstations().execute();

		workstationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String ID = ((TextView) view.findViewById(android.R.id.text1)).
						getText().toString();
				String processName = ((TextView) view.findViewById(android.R.id.text2)).
						getText().toString();

				Intent openWorkstationIntent = new Intent(getApplicationContext(),
						OpenWorkstationActivity.class);

				openWorkstationIntent.putExtra("ID", ID);
				openWorkstationIntent.putExtra("processName", processName);

				startActivity(openWorkstationIntent);
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
				updateWorkstationsListView(workstationsList);
				return true;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				updateWorkstationsListView(searchWorkstations(query));
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				updateWorkstationsListView(searchWorkstations(newText));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	private void updateWorkstationsListView(ArrayList<HashMap<String, String>> updatedList)
	{
		ListAdapter adapter = new SimpleAdapter(WorkstationListActivity.this,
				updatedList,
				android.R.layout.simple_list_item_2,
				new String[] { "ID", "name" },
				new int[] {android.R.id.text1, android.R.id.text2});
		workstationsListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> searchWorkstations(String query)
	{
		ArrayList<HashMap<String, String>> result = new ArrayList<>();

		for (HashMap<String, String> workstation : workstationsList)
		{
			if (workstation.get("name").toLowerCase().contains(query.toLowerCase()) ||
				workstation.get("ID").toLowerCase().contains(query.toLowerCase()))
				result.add(workstation);
		}

		return result;
	}

	private class LoadAllWorkstations extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			JSONArray jsonWorkstations;
			try
			{
				jsonWorkstations = dataManager.getDataFromDB(
						"SELECT Workstation.ID, name " +
								"FROM Workstation JOIN Process " +
								"ON Workstation.process_id=Process.ID " +
								"ORDER BY Workstation.ID;");

				for (int i = 0; i < jsonWorkstations.length(); i++)
					workstationsList.add(dataManager.jsonToHashMap(
							jsonWorkstations.getJSONObject(i)));
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

			if (workstationsList.size() == 0)
			{
				Toast.makeText(WorkstationListActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			ListAdapter adapter = new SimpleAdapter(WorkstationListActivity.this,
					workstationsList,
					android.R.layout.simple_list_item_2,
					new String[] { "ID", "name" },
					new int[] {android.R.id.text1, android.R.id.text2});
			workstationsListView.setAdapter(adapter);
		}
	}
}
