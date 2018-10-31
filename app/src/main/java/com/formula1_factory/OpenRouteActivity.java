package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;

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

		Intent sourceIntent = getIntent();

		dataManager = new DataManager();
		routeInfo = new HashMap<>();
		componentsList = new ArrayList<>();
		processesList = new ArrayList<>();

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
				jsonRouteInfo = dataManager.getDataFromDB("");
				jsonProcessesList = dataManager.getDataFromDB("");
				jsonComponentList = dataManager.getDataFromDB("");

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
		}
	}
}
