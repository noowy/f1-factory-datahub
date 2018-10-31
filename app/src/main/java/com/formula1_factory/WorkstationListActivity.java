package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class WorkstationListActivity extends AppCompatActivity
{
	private ListView workstationsListView;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> workstationsList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workstation_list);

		workstationsListView = (ListView) findViewById(R.id.workstations_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

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
