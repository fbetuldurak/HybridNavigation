package com.example.hybridnavigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import android.net.ParseException;
import android.util.Base64;
import android.util.Log;

public class Result {

	//private String ID; 
	public Stack lifo = new Stack();
	//public Stack lifoDirections = new Stack();
	private String db; 
	//private String rev;
	private String workOutputID;
	private String workAssignmentID; //task ID
	public ArrayList<String> resultlist; //the results with ArrayList
	private JSONObject jsonresult; //the result with Json format
	public HashMap<String, String> hashMapdirectionsteps ;
	public ArrayList<String> directionsteps;

	public Result(/*String ID, */ String db, String workAssignmentID, String workOutputID ) {
		//this.ID = ID;
		this.db = db;
		this.workAssignmentID = workAssignmentID;
		this.workOutputID = workOutputID;
		this.resultlist = new ArrayList<String>();
		this.directionsteps = new ArrayList<String>();
		this.hashMapdirectionsteps  = new HashMap<String, String>();
		this.jsonresult =new JSONObject();
		//this.rev = getRev(this.ID,this.db);
	}


	


	public void clear(){
		
		while ( !this.lifo.empty() )
        {
                this.lifo.pop();
                
        }
		
	
		this.resultlist.clear();
		//this.directionsteps.clear();
		
	}
	
	public void delete(){
		int a=resultlist.size();
		int b=(Integer) this.lifo.pop();
		//int c=(Integer) this.lifoDirections.pop();
		//int d= directionsteps.size();
		
		for (int i=a; i>=a-b+1; i--){
			this.resultlist.remove(i-1);
			
		}
		
			
	}


	public void insertIntoDb(){
		JSONArray auth = new JSONArray();
		auth.put("joe");
		auth.put("parveen");
		auth.put("broker");
		
		try {
			//jsonresult.accumulate("path", resultlist);
			jsonresult.accumulate("results", hashMapdirectionsteps);
			//jsonresult.put("_id", UUID.randomUUID().toString());
			//jsonresult.put("_rev", this.rev);
			jsonresult.put("type", "WorkOutput");
			jsonresult.put("workAssignmentID", this.workAssignmentID);
			//Log.d("workAssignmentID", this.workAssignmentID);
			
			jsonresult.put("workOutputID", this.workOutputID);
			jsonresult.put("authors", auth);
		} catch (JSONException e) {
			
			e.printStackTrace();
		}

		Runnable runnable = new Runnable() {
			public void run() {
		
		HttpURLConnection con2 = null;
		try {
			con2 = (HttpURLConnection) new URL("http://hpc.iriscouch.com:5984/" +db).openConnection();
		} catch (MalformedURLException e2) {
			
			e2.printStackTrace();
		} catch (IOException e2) {
			
			e2.printStackTrace();
		}
		con2.setDoOutput(true);

		String encoded = Base64.encodeToString("joe:hpc1234".getBytes(), Base64.NO_WRAP); 
		con2.setRequestProperty("Authorization", "Basic "+encoded);
		try {
			con2.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			
			e1.printStackTrace();
		}
		con2.setRequestProperty("Content-Type", "application/json");
		try {
			con2.connect();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		try {
			OutputStream os = con2.getOutputStream();
			os.write(jsonresult.toString().getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		try {
			System.out.println(con2.getInputStream().toString());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
			} //end of run()
			
		};
		
		Thread result_thread = new Thread(runnable);
		result_thread.start();
	

	}



}
