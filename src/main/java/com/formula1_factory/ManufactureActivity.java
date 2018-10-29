package com.formula1_factory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ManufactureActivity extends AppCompatActivity
{
	Button factoryOrdersButton;
	Button componentListButton;
	Button routesButton;
	Button workstationsListButton;
	Button marketOrdersButton;
	Button marketButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manufacture);

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
