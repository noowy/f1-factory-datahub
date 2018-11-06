package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

public class OpenRouteActivity extends AppCompatActivity
{
	private EditText routeIDField;
	private EditText routeNameField;
	private EditText descriptionField;
	private EditText completionTimeField;
	private EditText productNameField;
	private ListView processesListView;
	private ListView componentsListView;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private HashMap<String, String> routeInfo;
	private ArrayList<HashMap<String, String>> componentsList;
	private ArrayList<HashMap<String, String>> processesList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_route);

		routeIDField = (EditText) findViewById(R.id.route_id_field);
		routeNameField = (EditText) findViewById(R.id.route_name_field);
		descriptionField = (EditText) findViewById(R.id.description_field);
		completionTimeField = (EditText) findViewById(R.id.completion_time_field);
		productNameField = (EditText) findViewById(R.id.product_name_field);
		processesListView = (ListView) findViewById(R.id.processes_listview);
		componentsListView = (ListView) findViewById(R.id.components_listview);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		dataManager = new DataManager();
		routeInfo = new HashMap<>();
		componentsList = new ArrayList<>();
		processesList = new ArrayList<>();

		Intent sourceIntent = getIntent();
		routeInfo.put("routeName", sourceIntent.getStringExtra("routeName"));
		routeInfo.put("routeID", sourceIntent.getStringExtra("routeID"));
		new LoadRoute().execute();

	}

	class LoadRoute extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			JSONArray jsonRouteInfo;
			JSONArray jsonProcessesList;
			JSONArray jsonComponentList;

			try
			{
				jsonRouteInfo = dataManager.getDataFromDB(
						"SELECT Route.description, " +
									"(SUM(Route_Process.transportation_time) + " +
										"SUM(Process.completion_time)) AS completion_time, " +
									"Detail.name AS detail_name " +
								"FROM Route " +
								"JOIN Route_Process " +
								"ON Route_Process.route_id=Route.ID " +
								"JOIN Process " +
								"ON Route_Process.process_id=Process.ID " +
								"JOIN Detail " +
								"ON Detail.ID=" +
									"(SELECT DISTINCT product_id " +
									"FROM Specification " +
									"WHERE route_id=" + routeInfo.get("routeID") + ") " +
								"WHERE Route.ID=" + routeInfo.get("routeID") + ";");
				jsonProcessesList = dataManager.getDataFromDB(
						"SELECT Process.name " +
								"FROM Process " +
								"JOIN Route_Process " +
								"ON Process.ID=Route_Process.process_id " +
								"WHERE route_id=" + routeInfo.get("routeID") + ";");
				jsonComponentList = dataManager.getDataFromDB(
						"SELECT Detail.name, component_quantity " +
								"FROM Detail " +
								"JOIN Specification " +
								"ON Detail.ID=Specification.component_id " +
								"WHERE route_id=" + routeInfo.get("routeID") + ";");

				routeInfo.putAll(dataManager.jsonToHashMap(jsonRouteInfo.getJSONObject(0)));

				for (int i = 0; i < jsonProcessesList.length(); i++)
				{
					processesList.add(dataManager.jsonToHashMap(
							jsonProcessesList.getJSONObject(i)));
				}

				for (int i = 0; i < jsonComponentList.length(); i++)
				{
					componentsList.add(dataManager.jsonToHashMap(
							jsonComponentList.getJSONObject(i)));
				}

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
				Toast.makeText(OpenRouteActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_SHORT).show();
				return;
			}

			routeIDField.setText(routeInfo.get("routeID"));
			routeNameField.setText(routeInfo.get("routeName"));
			descriptionField.setText(routeInfo.get("description"));
			completionTimeField.setText(routeInfo.get("completion_time") + " minutes");
			productNameField.setText(routeInfo.get("detail_name"));

			ListAdapter componentsAdapter = new SimpleAdapter(OpenRouteActivity.this,
					componentsList,
					android.R.layout.simple_list_item_2,
					new String[] { "name", "component_quantity" },
					new int[] { android.R.id.text1, android.R.id.text2 });
			ListAdapter processesAdapter = new SimpleAdapter(OpenRouteActivity.this,
					processesList,
					android.R.layout.simple_list_item_1,
					new String[] { "name" },
					new int[] { android.R.id.text1 });

			componentsListView.setAdapter(componentsAdapter);
			processesListView.setAdapter(processesAdapter);
		}
	}
}
