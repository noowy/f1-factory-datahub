package com.formula1_factory;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.HashMap;

public class OpenWorkstationActivity extends AppCompatActivity
{
	private EditText workstationIDField;
	private EditText descriptionField;
	private EditText processNameField;
	private ProgressBar loadingCircle;

	private DataManager dataManager;

	private HashMap<String, String> workstationInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_workstation);

		workstationIDField = (EditText) findViewById(R.id.workstation_id_field);
		descriptionField = (EditText) findViewById(R.id.description_field);
		processNameField = (EditText) findViewById(R.id.process_name_field);
		loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

		dataManager = new DataManager();
		workstationInfo = new HashMap<>();

		Intent sourceIntent = getIntent();
		workstationInfo.put("ID", sourceIntent.getStringExtra("ID"));
		workstationInfo.put("processName", sourceIntent.getStringExtra("processName"));
		new LoadWorkstation().execute();
	}

	class LoadWorkstation extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected void onPreExecute()
		{
			loadingCircle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			JSONArray jsonWorkstationInfo;

			try
			{
				jsonWorkstationInfo = dataManager.getDataFromDB(
						"SELECT description " +
								"FROM Workstation " +
								"WHERE ID=" + workstationInfo.get("ID") + "");
				workstationInfo.putAll(dataManager.jsonToHashMap(
						jsonWorkstationInfo.getJSONObject(0)));
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
				Toast.makeText(OpenWorkstationActivity.this,
						R.string.server_unavailable,
						Toast.LENGTH_LONG).show();
				return;
			}

			workstationIDField.setText(workstationInfo.get("ID"));
			descriptionField.setText(workstationInfo.get("description"));
			processNameField.setText(workstationInfo.get("processName"));
		}
	}
}
