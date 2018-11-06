package com.formula1_factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.*;

//  DataManager class is responsible for obtaining and changing data in
//  in the remote DB through the http connection. Returns data in JSONs


public class DataManager
{
	private String dbAddress;
	private String username;
	private String password;

	DataManager()
	{
		dbAddress = "http://192.168.0.102/index.php"; // 192.168.43.197 for wifi hotspot
		username = "f1drm";
		password = "4563728deK";
	}

	DataManager(String dbAddress, String username, String password)
	{
		this.dbAddress = dbAddress;
		this.username = username;
		this.password = password;

	}

	public JSONArray getDataFromDB(String query) throws Exception
	{
		JSONObject result = null;

		result = makeQueryToDB(query);

		if (result == null)
			return null;

		if (!result.getBoolean("success"))
			return null;

		return result.getJSONArray("content");
	}

	public boolean saveDataToDB(String query) throws Exception
	{
		JSONObject result;
		result = makeQueryToDB(query);

		return result != null && result.getBoolean("success");
	}

	public JSONArray saveDataToDB(String query, HashMap<String, String> params) throws Exception
	{
		JSONObject result;
		result = makeQueryToDB(query, params);

		if (result == null)
			return null;

		if (!result.getBoolean("success"))
			return null;

		return result.getJSONArray("content");
	}

	public boolean updateDataInDB(String query) throws Exception
	{
		JSONObject result;
		result = makeQueryToDB(query);

		return result != null && result.getBoolean("success");
	}

	public boolean deleteDataFromDB(String query) throws Exception
	{
		JSONObject result;
		result = makeQueryToDB(query);

		return result != null && result.getBoolean("success");
	}

	public HashMap<String, String> jsonToHashMap(JSONObject jsonObject)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		JSONArray jsonKeys = jsonObject.names();

		for (int i = 0; i < jsonKeys.length(); i++)
		{
			String key = jsonKeys.optString(i);

			if (key.equals("quantity") && jsonObject.optString(key).equals("null"))
			{
				result.put(key, "0");
				continue;
			}

			if (key.equals("description") && jsonObject.optString(key).equals("null"))
			{
				result.put(key, "");
				continue;
			}

			result.put(key, jsonObject.optString(key));
		}

		return result;
	}

	private JSONObject makeQueryToDB(String query) throws Exception
	{
		if (query.length() == 0)
			return null;

		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		JSONObject result = null;

		URL dbURL = new URL(dbAddress);
		HttpURLConnection connection = (HttpURLConnection) dbURL.openConnection();
		connection.setDoOutput(true);

		try
		{
			OutputStreamWriter request = new OutputStreamWriter(
					connection.getOutputStream());
			request.write("query=" + encodedQuery +
					"&username=" + URLEncoder.encode(username, "UTF-8") +
					"&password=" + URLEncoder.encode(password, "UTF-8"));
			request.close();
		}
		catch (IOException e)
		{
			return new JSONObject().accumulate("success", false);
		}

		BufferedReader response = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		result = new JSONObject(convertToString(response));
		response.close();

		return result;
	}

	private JSONObject makeQueryToDB(String query, HashMap<String, String> params)
			throws Exception
	{
		if (query.length() == 0)
			return null;

		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		JSONObject result = null;

		URL dbURL = new URL(dbAddress);
		HttpURLConnection connection = (HttpURLConnection) dbURL.openConnection();
		connection.setDoOutput(true);

		try
		{
			OutputStreamWriter request = new OutputStreamWriter(
					connection.getOutputStream());
			request.write("query=" + encodedQuery +
					"&username=" + URLEncoder.encode(username, "UTF-8") +
					"&password=" + URLEncoder.encode(password, "UTF-8") +
					convertToHttpRequest(params, "UTF-8"));
			request.close();
		}
		catch (IOException e)
		{
			return new JSONObject().accumulate("success", false);
		}

		BufferedReader response = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		result = new JSONObject(convertToString(response));
		response.close();

		return result;
	}

	private String convertToString(BufferedReader reader) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null)
			builder.append(line);

		return builder.toString();
	}

	private String convertToHttpRequest(HashMap<String, String> params, String encoding)
	{
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> value : params.entrySet())
		{
			try
			{
				result.append("&" + value.getKey() + "=");
				result.append(URLEncoder.encode(value.getValue(), encoding));
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		return result.toString();
	}
}
