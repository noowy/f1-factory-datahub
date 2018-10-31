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

public class ComponentListActivity extends AppCompatActivity
{
	private Button orderButton;
	private ListView componentListView;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private ArrayList<HashMap<String, String>> componentList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_component_list);

		orderButton = (Button) findViewById(R.id.order_component_button);
		componentListView = (ListView) findViewById(R.id.component_list);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		dataManager = new DataManager();
		componentList = new ArrayList<HashMap<String, String>>();
		new LoadAllComponentsTask().execute();

		orderButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});

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

				Intent openComponentIntent = new Intent(getApplicationContext(),
						OpenComponentActivity.class);

				openComponentIntent.putExtra("ID", ID);
				openComponentIntent.putExtra("name", name);
				openComponentIntent.putExtra("quantity", quantity);
				openComponentIntent.putExtra("units", units);

				startActivity(openComponentIntent);
			}
		});
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
						"SELECT Detail.ID, name, quantity, units " +
								"FROM Detail LEFT OUTER JOIN Stock " +
								"ON Detail.ID=Stock.component_id " +
								"WHERE for_sale=false " +
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
					componentList, R.layout.component_list_item ,
					new String[] { "ID", "name", "quantity", "units" },
					new int[] { R.id.component_id,
							R.id.component_name,
							R.id.component_quantity,
							R.id.component_units });
			componentListView.setAdapter(adapter);
		}
	}
}
