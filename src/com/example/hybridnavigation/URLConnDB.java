package com.example.hybridnavigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class URLConnDB {
	
	public String dbName=null;
	public String doc_id=null;
	JSONObject jObject;
	
	public URLConnDB (String dbName, String doc_id){
		
		this.dbName = dbName;
		this.doc_id = doc_id;
		
		}
	
	public JSONObject getJson() {
	
	URL url = null ;
    try {
		url = new URL("http://hpc.iriscouch.com/"+dbName +"/" +doc_id);
	} catch (MalformedURLException e) {
		
		e.printStackTrace();
	}
    
    HttpURLConnection urlConnection = null;
	try {
		urlConnection = (HttpURLConnection) url.openConnection();
		BufferedReader in1 = null;
		in1 = new BufferedReader(new InputStreamReader (urlConnection.getInputStream()));
		String inputLine1;
		String json2="";
		while ((inputLine1 = in1.readLine()) != null){ 
				json2 = json2+inputLine1;
			}
		in1.close();

	
    //Log.d("Json", json2);	
	try {
		jObject = new JSONObject(json2);
	} catch (JSONException e) {
		
		e.printStackTrace();
	}
    } catch (IOException e) {
		
		e.printStackTrace();
	}
	return jObject;
	
	}

}
