package com.formula1_factory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class ManufactureActivity extends AppCompatActivity
{
	private Button factoryOrdersButton;
	private Button componentListButton;
	private Button routesButton;
	private Button workstationsListButton;
	private Button marketOrdersButton;
	private Button marketButton;
	private Toolbar appToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manufacture);

		appToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(appToolbar);

		factoryOrdersButton = (Button) findViewById(R.id.factory_orders_button);
		factoryOrdersButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), FactoryOrdersActivity.class);
				startActivity(intent);
			}
		});

		componentListButton = (Button) findViewById(R.id.component_list_button);
		componentListButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), ComponentListActivity.class);
				startActivity(intent);
			}
		});

		routesButton = (Button) findViewById(R.id.routes_button);
		routesButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), RoutesActivity.class);
				startActivity(intent);
			}
		});

		workstationsListButton = (Button) findViewById(R.id.workstations_list_button);
		workstationsListButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), WorkstationListActivity.class);
				startActivity(intent);
			}
		});

		marketOrdersButton = (Button) findViewById(R.id.market_orders_button);
		marketOrdersButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), MarketOrdersActivity.class);
				startActivity(intent);
			}
		});

		marketButton = (Button) findViewById(R.id.market_button);
		marketButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
//				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//				startActivity(intent);
				ManufactureActivity.this.onBackPressed();
			}
		});
	}
}
