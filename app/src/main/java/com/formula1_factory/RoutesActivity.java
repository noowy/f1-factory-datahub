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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutesActivity extends AppCompatActivity
{
	private ListView routesListView;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> routesList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);

		routesListView = (ListView) findViewById(R.id.routes_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

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
