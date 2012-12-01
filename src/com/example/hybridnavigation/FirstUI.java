package com.example.hybridnavigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

@SuppressLint("ParserError")
public class FirstUI extends Activity {

	private String doc_id=null;    //for workAssignment doc 
	private String rev_id=null;
	private String requestorID = null;
	private String assignmentStatusTimeStamp=null;
	private String authors=null;
	private String workerID= null;
	JSONObject jObject = null;
	private String workAssignmentID=null;
	private String workRequestID=null;
	private String workInputID=null;
	private URLConnDB connDB = null;
	private int statusflag = 0;
	private String workEstimateStatus=null;
	private Object lock1 = new Object();
	private Object lock2 = new Object();
	
	String skillsNeeded=null;
	TextView title;
	TextView skillText;
	EditText inputTime;
	EditText inputCost;
	Button btnSend;
	Button btnStart ;
	String timeInput;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_ui);

		title = (TextView) findViewById(R.id.title);
		skillText = (TextView) findViewById(R.id.skilltext);
		inputTime = (EditText) findViewById(R.id.timeinput);
		inputCost = (EditText) findViewById(R.id.costinput);
		btnStart = (Button) findViewById(R.id.start);
		btnSend = (Button) findViewById(R.id.send);

		title.setText("Press start to get your job");
		skillText.setText("skills needed\n1. skill 1\n2. skill 2\n");
		btnSend.setEnabled(false);




	}
	//Listening to button events

	public void onStartButtonClicked(View view){
		//Log.d("ButtonStart", "It is working");
		title.setText("Waiting for job");
		btnSend.setEnabled(true);
		btnStart.setEnabled(false);
		
		
		Runnable runnable = new Runnable(){
			public void run(){
				String json_Text2 = null;


				///////////
				// Start Listening the DB to read required skills(brokerdb listened for workAssignment type)
				HttpURLConnection con = null;

				try {
					con = (HttpURLConnection) new URL("http://hpc.iriscouch.com/brokerdb/_changes?feed=continous&heartbeat=10&includeDocs=true&filter=Worker/assignments_by_worker_id&workerID=joe").openConnection();
				} catch (MalformedURLException e1) {

					e1.printStackTrace();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				//con.setDoOutput(true);
				con.setUseCaches(false);
				con.setRequestProperty("Connection", "Keep-Alive"); 
				try {
					con.setRequestMethod("GET");
				} catch (ProtocolException e2) {

					e2.printStackTrace();
				}
				try {
					con.connect();
				} catch (IOException e2) {

					e2.printStackTrace();
				}
				//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
				BufferedReader in = null;
				try {
					in = new BufferedReader(  new InputStreamReader(con.getInputStream()));
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				//Read the data on jsonText1, it is JSon format
				String inputLine;
				String jsonText1="";

				try {
					while ((inputLine = in.readLine()) != null){ 
						jsonText1 = jsonText1+inputLine;
						//if(!inputLine.equals(""))
							//Log.d("Print the data from update", jsonText1);

					}
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//Log.d("Update from DB", jsonText1.trim());


				//Creates a parser to parse the data from JSon format
				JSONParser parser=new JSONParser();
				ContainerFactory containerFactory = new ContainerFactory(){
					public List creatArrayContainer() {
						return new LinkedList();
					}

					public Map createObjectContainer() {
						return new LinkedHashMap();
					}

				};
				//Maps the each key to values in each line iteratively          

				Map json = null;
				try {
					json = (Map)parser.parse(jsonText1, containerFactory);
				} catch (ParseException e) {

					e.printStackTrace();
				}

				Iterator iter = json.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry entry = (Map.Entry)iter.next();
					//Log.d("parsed key-values", entry.getValue().toString()); 

					///////////////// We will get the document id to read the document for workAssignmentID@workRequestID
					if (entry.getKey().toString().equals("results")){

						String jsonText2 = null;

						jsonText2 = entry.getValue().toString();
						//Log.d("jsonText2", jsonText2);
						int firstindx = jsonText2.indexOf("id=");
						int secondindx = jsonText2.indexOf("changes=");
						doc_id = jsonText2.substring(firstindx +3 , secondindx - 2);
						Log.d("doc_id", doc_id);

						con.disconnect(); // I close the connection here, but it might be closed under some other circumctances.
					} // end of if(entry.getKey().toString().equals("results")) 
				} //end of while

				////////////////
				////////////////Now I need to read this document

		//
				URL url = null;

				try {
					url = new URL("http://hpc.iriscouch.com/brokerdb/" +doc_id);


				} catch (MalformedURLException e) {

					e.printStackTrace();
				}
				HttpURLConnection urlConnection = null;
				try {
					urlConnection = (HttpURLConnection) url.openConnection();
				} catch (IOException e) {

					e.printStackTrace();
				}

				BufferedReader in1 = null;
				try {
					in1 = new BufferedReader(
							new InputStreamReader(
									urlConnection.getInputStream()));
				} catch (IOException e) {

					e.printStackTrace();
				}
				String inputLine1;
				String json2="";
				try {
					while ((inputLine1 = in1.readLine()) != null){ 
						json2 = json2+inputLine1;
					}
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					in1.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

				//Log.d("Json", json2);	


				try {
					jObject = new JSONObject(json2);
				} catch (JSONException e1) {

					e1.printStackTrace();
				}
				
				//connDB = new URLConnDB("brokerdb", doc_id);
				//jObject = connDB.getJson();

				try {
					rev_id = jObject.getString("_rev");
					requestorID = jObject.getString("requestorID");
					assignmentStatusTimeStamp = jObject.getString("assignmentStatusTimeStamp");
					authors = jObject.getString("authors");
					workerID = jObject.getString("workerID");
					workAssignmentID = jObject.getString("workAssignmentID");
					workRequestID = jObject.getString("workRequestID");
					//Log.d("workAssignment", workAssignmentID);
					//Log.d("workRequest", workRequestID);


				} catch (JSONException e1) {

					e1.printStackTrace();
				}
				///////////////////End of reading the document             

				///////////////////// Now go to query on workRequestID  to get requiredskills
				HttpURLConnection connect1 = null;


				try {
					connect1 = (HttpURLConnection) new URL("http://hpc.iriscouch.com/brokerdb/_design/WorkRequest/_view/by_requestID?key=\""+workRequestID+"\"").openConnection();
				} catch (MalformedURLException e1) {

					e1.printStackTrace();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				//connect1.setDoOutput(true);
				connect1.setUseCaches(false);
				connect1.setRequestProperty("Connection", "Keep-Alive"); 
				try {
					connect1.setRequestMethod("GET");
				} catch (ProtocolException e2) {

					e2.printStackTrace();
				}
				try {
					connect1.connect();
				} catch (IOException e2) {

					e2.printStackTrace();
				} 
				//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
				BufferedReader buffreader = null;
				try {
					buffreader = new BufferedReader(  new InputStreamReader(connect1.getInputStream()));
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				//Read the data on jsonText1, it is JSon format
				String inputLine2;
				String json_Text1="";

				try {
					while ((inputLine2 = buffreader.readLine()) != null){ 
						json_Text1 = json_Text1+inputLine2;
						//if(!inputLine2.equals(""))
							// System.out.println(line);
							//Log.d("2nd connection_results", json_Text1);

					}
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				try {
					buffreader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				JSONObject jObject1=null;
				try {
					jObject1= new JSONObject(json_Text1);
				} catch (JSONException e2) {

					e2.printStackTrace();
				}

				try {
					json_Text2 = jObject1.getString("rows");
					Log.d("rows", json_Text2);
				} catch (JSONException e2) {
					e2.printStackTrace();
				}

				int firstindx = json_Text2.indexOf("value\":");
				//Log.d("pay attention here", String.valueOf(firstindx));
				int secondindx = json_Text2.indexOf("id\":");
				json_Text2 = json_Text2.substring(firstindx +8 , secondindx - 3);
				//Log.d("surprise", json_Text2);




				////////////////Read required skills

				URL url1 = null;

				try {
					url1 = new URL("http://hpc.iriscouch.com/brokerdb/" +json_Text2);


				} catch (MalformedURLException e) {

					e.printStackTrace();
				}
				HttpURLConnection urlConnection1 = null;
				try {
					urlConnection1 = (HttpURLConnection) url1.openConnection();
				} catch (IOException e) {

					e.printStackTrace();
				}

				BufferedReader in12 = null;
				try {
					in12 = new BufferedReader(
							new InputStreamReader(
									urlConnection1.getInputStream()));
				} catch (IOException e) {

					e.printStackTrace();
				}
				String inputLine12;
				String json23="";
				try {
					while ((inputLine12 = in12.readLine()) != null){ 
						json23 = json23+inputLine12;
					}
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					in12.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

				//Log.d("Json", json23);	


				try {
					jObject = new JSONObject(json23);
				} catch (JSONException e1) {

					e1.printStackTrace();
				}
				//String skillsNeeded=null;

				try {
					skillsNeeded= jObject.getString("skillsNeeded");

					//Log.d("skillsNeeded", skillsNeeded);

					//skillText.setText(skillsNeeded);

				} catch (JSONException e1) {

					e1.printStackTrace();
				}
				///////////////////End of reading the document        

				////////////

				
				
			}
			
		};
		
		Thread T = new Thread(runnable);
		T.start();
		try {
			T.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		skillText.setText(skillsNeeded);
		
//		String json_Text2 = null;
//
//
//		///////////
//		// Start Listening the DB to read required skills(brokerdb listened for workAssignment type)
//		HttpURLConnection con = null;
//
//		try {
//			con = (HttpURLConnection) new URL("http://hpc.iriscouch.com/brokerdb/_changes?feed=continous&heartbeat=10&includeDocs=true&filter=Worker/assignments_by_worker_id&workerID=joe").openConnection();
//		} catch (MalformedURLException e1) {
//
//			e1.printStackTrace();
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		con.setDoOutput(true);
//		con.setUseCaches(false);
//		con.setRequestProperty("Connection", "Keep-Alive"); 
//		try {
//			con.setRequestMethod("GET");
//		} catch (ProtocolException e2) {
//
//			e2.printStackTrace();
//		}
//		try {
//			con.connect();
//		} catch (IOException e2) {
//
//			e2.printStackTrace();
//		}
//		//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(  new InputStreamReader(con.getInputStream()));
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		//Read the data on jsonText1, it is JSon format
//		String inputLine;
//		String jsonText1="";
//
//		try {
//			while ((inputLine = in.readLine()) != null){ 
//				jsonText1 = jsonText1+inputLine;
//				//if(!inputLine.equals(""))
//					//Log.d("Print the data from update", jsonText1);
//
//			}
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		try {
//			in.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		//Log.d("Update from DB", jsonText1.trim());
//
//
//		//Creates a parser to parse the data from JSon format
//		JSONParser parser=new JSONParser();
//		ContainerFactory containerFactory = new ContainerFactory(){
//			public List creatArrayContainer() {
//				return new LinkedList();
//			}
//
//			public Map createObjectContainer() {
//				return new LinkedHashMap();
//			}
//
//		};
//		//Maps the each key to values in each line iteratively          
//
//		Map json = null;
//		try {
//			json = (Map)parser.parse(jsonText1, containerFactory);
//		} catch (ParseException e) {
//
//			e.printStackTrace();
//		}
//
//		Iterator iter = json.entrySet().iterator();
//		while(iter.hasNext()){
//			Map.Entry entry = (Map.Entry)iter.next();
//			//Log.d("parsed key-values", entry.getValue().toString()); 
//
//			///////////////// We will get the document id to read the document for workAssignmentID@workRequestID
//			if (entry.getKey().toString().equals("results")){
//
//				String jsonText2 = null;
//
//				jsonText2 = entry.getValue().toString();
//				//Log.d("jsonText2", jsonText2);
//				int firstindx = jsonText2.indexOf("id=");
//				int secondindx = jsonText2.indexOf("changes=");
//				doc_id = jsonText2.substring(firstindx +3 , secondindx - 2);
//				Log.d("doc_id", doc_id);
//
//				con.disconnect(); // I close the connection here, but it might be closed under some other circumctances.
//			} // end of if(entry.getKey().toString().equals("results")) 
//		} //end of while
//
//		////////////////
//		////////////////Now I need to read this document
//
////
//		URL url = null;
//
//		try {
//			url = new URL("http://hpc.iriscouch.com/brokerdb/" +doc_id);
//
//
//		} catch (MalformedURLException e) {
//
//			e.printStackTrace();
//		}
//		HttpURLConnection urlConnection = null;
//		try {
//			urlConnection = (HttpURLConnection) url.openConnection();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		BufferedReader in1 = null;
//		try {
//			in1 = new BufferedReader(
//					new InputStreamReader(
//							urlConnection.getInputStream()));
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		String inputLine1;
//		String json2="";
//		try {
//			while ((inputLine1 = in1.readLine()) != null){ 
//				json2 = json2+inputLine1;
//			}
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		try {
//			in1.close();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		//Log.d("Json", json2);	
//
//
//		try {
//			jObject = new JSONObject(json2);
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//		
//		//connDB = new URLConnDB("brokerdb", doc_id);
//		//jObject = connDB.getJson();
//
//		try {
//			rev_id = jObject.getString("_rev");
//			requestorID = jObject.getString("requestorID");
//			assignmentStatusTimeStamp = jObject.getString("assignmentStatusTimeStamp");
//			authors = jObject.getString("authors");
//			workerID = jObject.getString("workerID");
//			workAssignmentID = jObject.getString("workAssignmentID");
//			workRequestID = jObject.getString("workRequestID");
//			//Log.d("workAssignment", workAssignmentID);
//			//Log.d("workRequest", workRequestID);
//
//
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//		///////////////////End of reading the document             
//
//		///////////////////// Now go to query on workRequestID  to get requiredskills
//		HttpURLConnection connect1 = null;
//
//
//		try {
//			connect1 = (HttpURLConnection) new URL("http://hpc.iriscouch.com/brokerdb/_design/WorkRequest/_view/by_requestID?key=\""+workRequestID+"\"").openConnection();
//		} catch (MalformedURLException e1) {
//
//			e1.printStackTrace();
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		connect1.setDoOutput(true);
//		connect1.setUseCaches(false);
//		connect1.setRequestProperty("Connection", "Keep-Alive"); 
//		try {
//			connect1.setRequestMethod("GET");
//		} catch (ProtocolException e2) {
//
//			e2.printStackTrace();
//		}
//		try {
//			connect1.connect();
//		} catch (IOException e2) {
//
//			e2.printStackTrace();
//		} 
//		//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
//		BufferedReader buffreader = null;
//		try {
//			buffreader = new BufferedReader(  new InputStreamReader(connect1.getInputStream()));
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		//Read the data on jsonText1, it is JSon format
//		String inputLine2;
//		String json_Text1="";
//
//		try {
//			while ((inputLine2 = buffreader.readLine()) != null){ 
//				json_Text1 = json_Text1+inputLine2;
//				//if(!inputLine2.equals(""))
//					// System.out.println(line);
//					//Log.d("2nd connection_results", json_Text1);
//
//			}
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		try {
//			buffreader.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//
//		JSONObject jObject1=null;
//		try {
//			jObject1= new JSONObject(json_Text1);
//		} catch (JSONException e2) {
//
//			e2.printStackTrace();
//		}
//
//		try {
//			json_Text2 = jObject1.getString("rows");
//			Log.d("rows", json_Text2);
//		} catch (JSONException e2) {
//			e2.printStackTrace();
//		}
//
//		int firstindx = json_Text2.indexOf("value\":");
//		//Log.d("pay attention here", String.valueOf(firstindx));
//		int secondindx = json_Text2.indexOf("id\":");
//		json_Text2 = json_Text2.substring(firstindx +8 , secondindx - 3);
//		//Log.d("surprise", json_Text2);
//
//
//
//
//		////////////////Read required skills
//
//		URL url1 = null;
//
//		try {
//			url1 = new URL("http://hpc.iriscouch.com/brokerdb/" +json_Text2);
//
//
//		} catch (MalformedURLException e) {
//
//			e.printStackTrace();
//		}
//		HttpURLConnection urlConnection1 = null;
//		try {
//			urlConnection1 = (HttpURLConnection) url1.openConnection();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		BufferedReader in12 = null;
//		try {
//			in12 = new BufferedReader(
//					new InputStreamReader(
//							urlConnection1.getInputStream()));
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		String inputLine12;
//		String json23="";
//		try {
//			while ((inputLine12 = in12.readLine()) != null){ 
//				json23 = json23+inputLine12;
//			}
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		try {
//			in12.close();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		//Log.d("Json", json23);	
//
//
//		try {
//			jObject = new JSONObject(json23);
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//		String skillsNeeded=null;
//
//		try {
//			skillsNeeded= jObject.getString("skillsNeeded");
//
//			//Log.d("skillsNeeded", skillsNeeded);
//
//			skillText.setText(skillsNeeded);
//
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//		///////////////////End of reading the document        
//
//		////////////

		

	}

	public void onSendButtonClicked(View view){
	//	Log.d("ButtonSend", " is also working");
		

		
		timeInput = inputTime.getText().toString();
		if(timeInput.isEmpty() || inputCost.getText().toString().isEmpty())
		{
			//Log.d("the input is empty", "empty"); // show me this 
			
			AlertDialog alertDialog = new AlertDialog.Builder(
               this).create();

    // Setting Dialog Title
    alertDialog.setTitle("Invalid Request");

    // Setting Dialog Message
    alertDialog.setMessage("Enter a valid Estimate");

     // Setting OK Button
    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface arg0, int arg1) {
    	arg0.cancel();       
    	//launchIntent();
    	}	
    	 });

    // Showing Alert Message
    alertDialog.show();
	//Log.d("where?", "after set up");

		} else if(!isNumber(timeInput)) {
			
			AlertDialog alertDialog = new AlertDialog.Builder(
		               this).create();

		    // Setting Dialog Title
		    alertDialog.setTitle("Invalid Request");

		    // Setting Dialog Message
		    alertDialog.setMessage("Estimate should be minutes");

		     // Setting OK Button
		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface arg0, int arg1) {
		    	arg0.cancel();       
		    	//launchIntent();
		    	}	
		    	 });

		    // Showing Alert Message
		    alertDialog.show();
		}
		
		else{

		title.setText("Response sent");
		btnStart.setEnabled(true);
		btnSend.setEnabled(false);
		int jsonSeq = 0 ;
		int flag=0;
		final String encoded = Base64.encodeToString("joe:hpc1234".getBytes(), Base64.NO_WRAP);
		
		Runnable runnable = new Runnable(){
			public void run(){
				////////Start to update DB 
				URL url = null;
				//Log.d("doc_id", doc_id);
				try {
					url = new URL("http://hpc.iriscouch.com/brokerdb/" +doc_id);


				} catch (MalformedURLException e) {

					e.printStackTrace();
				}
				HttpURLConnection urlConnection = null;
				try {
					urlConnection = (HttpURLConnection) url.openConnection();
				} catch (IOException e) {

					e.printStackTrace();
				}

				BufferedReader in1 = null;
				try {
					in1 = new BufferedReader(
							new InputStreamReader(
									urlConnection.getInputStream()));
				} catch (IOException e) {

					e.printStackTrace();
				}
				String inputLine1;
				String json2="";
				try {
					while ((inputLine1 = in1.readLine()) != null){ 
						json2 = json2+inputLine1;
					}
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					in1.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

				//Log.d("Json", json2);	


				try {
					jObject = new JSONObject(json2);
				} catch (JSONException e1) {

					e1.printStackTrace();
				}
				
				//connDB = new URLConnDB("brokerdb", doc_id);
				//jObject = connDB.getJson();

				//Log.d("just before putting docs", jObject.toString());
//				Calendar c = Calendar.getInstance();
//				c.setTimeInMillis(System.currentTimeMillis());
//				Date d = c.getTime();
				
				
				Date dNow = new Date( );
			    SimpleDateFormat ft =  new SimpleDateFormat ("yyyy.MM.dd 'at' hh:mm:ss a zzz");
				
				try {

					
					//////////
					jObject.put("workerID", "joe");
					jObject.put("requestorID", "parveen");
					jObject.put("workRequestID",workRequestID);
					//jObject.put("workRequestID", "331ad9b4-9f08-48a9-9c82-43f2ec758be0");
					jObject.put( "type", "WorkAssignment");
					jObject.put("workAssignmentID", workAssignmentID);
					//jObject.put("workAssignmentID", "ea236d36-3980-4d5e-a7ba-a20f362f9e32");
					//jObject.put( "authors", "[\"joe\",\"parveen\",\"broker\"]");
					//jObject.put("authors", authors);
					//jObject.put("assignmentStatusTimeStamp", "2012-08-27 00:18:09.258");
					jObject.put("estimateTime", inputTime.getText().toString());
					jObject.put("estimateCost", inputCost.getText().toString());
					jObject.put( "estimateStatus","waiting");
					jObject.put( "assignmentStatus","estimated");
					jObject.put( "estimateStatusTimeStamp", ft.format(dNow));
					//jObject.put( "estimateStatusTimeStamp", d.getYear()+"-"+d.getMonth()+"-"+d.getDate()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds());
				
			
					//jObject.put( "authors","joe");
					
					
					
					
				} catch (JSONException e1) {

					e1.printStackTrace();
				}

				//Log.d("jObject", jObject.toString());
				HttpURLConnection con2 = null;
				try {
					con2 = (HttpURLConnection) new URL("http://hpc.iriscouch.com:5984/brokerdb/"+doc_id).openConnection();
				} catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				con2.setDoOutput(true);
				try {
					con2.setRequestMethod("PUT");
				} catch (ProtocolException e1) {

					e1.printStackTrace();
				}
				//String encoded = Base64.encodeToString("joe:hpc1234".getBytes(), Base64.NO_WRAP); 
				con2.setRequestProperty("Authorization", "Basic "+encoded);
				con2.setRequestProperty("Content-Type", "application/json");
				try {
					con2.connect();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				try {
					OutputStream os = con2.getOutputStream();
					os.write(jObject.toString().getBytes());
					os.flush();
					os.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					//Log.d("Msg", "before input stream");
					//Log.d("msg",con2.getInputStream().toString());
					System.out.println(con2.getInputStream().toString());
					//Log.d("Msg", "after input stream");
				} catch (IOException e) {

					e.printStackTrace();
				}
				con2.disconnect();
				// end of update
			}
		};
		
		Thread T = new Thread(runnable);
		T.start();
		try {
			T.join();
		} catch (InterruptedException e3) {
	
			e3.printStackTrace();
		}
		
		

//		////////Start to update DB 
//		URL url = null;
//		//Log.d("doc_id", doc_id);
//		try {
//			url = new URL("http://hpc.iriscouch.com/brokerdb/" +doc_id);
//
//
//		} catch (MalformedURLException e) {
//
//			e.printStackTrace();
//		}
//		HttpURLConnection urlConnection = null;
//		try {
//			urlConnection = (HttpURLConnection) url.openConnection();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		BufferedReader in1 = null;
//		try {
//			in1 = new BufferedReader(
//					new InputStreamReader(
//							urlConnection.getInputStream()));
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		String inputLine1;
//		String json2="";
//		try {
//			while ((inputLine1 = in1.readLine()) != null){ 
//				json2 = json2+inputLine1;
//			}
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		try {
//			in1.close();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//
//		//Log.d("Json", json2);	
//
//
//		try {
//			jObject = new JSONObject(json2);
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//		
//		//connDB = new URLConnDB("brokerdb", doc_id);
//		//jObject = connDB.getJson();
//
//		//Log.d("just before putting docs", jObject.toString());
////		Calendar c = Calendar.getInstance();
////		c.setTimeInMillis(System.currentTimeMillis());
////		Date d = c.getTime();
//		
//		
//		Date dNow = new Date( );
//	    SimpleDateFormat ft =  new SimpleDateFormat ("yyyy.MM.dd 'at' hh:mm:ss a zzz");
//		
//		try {
//
//			
//			//////////
//			jObject.put("workerID", "joe");
//			jObject.put("requestorID", "parveen");
//			jObject.put("workRequestID",workRequestID);
//			//jObject.put("workRequestID", "331ad9b4-9f08-48a9-9c82-43f2ec758be0");
//			jObject.put( "type", "WorkAssignment");
//			jObject.put("workAssignmentID", workAssignmentID);
//			//jObject.put("workAssignmentID", "ea236d36-3980-4d5e-a7ba-a20f362f9e32");
//			//jObject.put( "authors", "[\"joe\",\"parveen\",\"broker\"]");
//			//jObject.put("authors", authors);
//			//jObject.put("assignmentStatusTimeStamp", "2012-08-27 00:18:09.258");
//			jObject.put("estimateTime", inputTime.getText().toString());
//			jObject.put("estimateCost", inputCost.getText().toString());
//			jObject.put( "estimateStatus","waiting");
//			jObject.put( "assignmentStatus","estimated");
//			jObject.put( "estimateStatusTimeStamp", ft.format(dNow));
//			//jObject.put( "estimateStatusTimeStamp", d.getYear()+"-"+d.getMonth()+"-"+d.getDate()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds());
//		
//	
//			//jObject.put( "authors","joe");
//			
//			
//			
//			
//		} catch (JSONException e1) {
//
//			e1.printStackTrace();
//		}
//
//		//Log.d("jObject", jObject.toString());
//		HttpURLConnection con2 = null;
//		try {
//			con2 = (HttpURLConnection) new URL("http://hpc.iriscouch.com:5984/brokerdb/"+doc_id).openConnection();
//		} catch (MalformedURLException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		con2.setDoOutput(true);
//		try {
//			con2.setRequestMethod("PUT");
//		} catch (ProtocolException e1) {
//
//			e1.printStackTrace();
//		}
//		String encoded = Base64.encodeToString("joe:hpc1234".getBytes(), Base64.NO_WRAP); 
//		con2.setRequestProperty("Authorization", "Basic "+encoded);
//		con2.setRequestProperty("Content-Type", "application/json");
//		try {
//			con2.connect();
//		} catch (IOException e1) {
//
//			e1.printStackTrace();
//		}
//		try {
//			OutputStream os = con2.getOutputStream();
//			os.write(this.jObject.toString().getBytes());
//			os.flush();
//			os.close();
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		try {
//			//Log.d("Msg", "before input stream");
//			//Log.d("msg",con2.getInputStream().toString());
//			System.out.println(con2.getInputStream().toString());
//			//Log.d("Msg", "after input stream");
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		con2.disconnect();
//		// end of update
		
	         

		/////now starting new activity!
		

		// Start Listening the DB to read required skills(brokerdb listened for workAssignment type)
		while (flag == 0) {   //means the Status is waiting

			
			Log.d("Msg", "inside while");
			
			Runnable runnable1 = new Runnable(){
				
				public void run() {

			HttpURLConnection con = null;

			try {
				con = (HttpURLConnection) new URL("http://hpc.iriscouch.com:5984/brokerdb/_changes?feed=continous&heartbeat=10&includeDocs=true&filter=Worker/assignments_by_request_id&requestID="+workRequestID).openConnection();
			} catch (MalformedURLException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			//con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Connection", "Keep-Alive"); 
			con.setRequestProperty("Authorization", "Basic "+encoded);
			try {
				con.setRequestMethod("GET");
			} catch (ProtocolException e2) {

				e2.printStackTrace();
			}
			try {
				con.connect();
			} catch (IOException e2) {

				e2.printStackTrace();
			}
			//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
			BufferedReader in = null;
			try {
				in = new BufferedReader(  new InputStreamReader(con.getInputStream()));
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			//Read the data on jsonText1, it is JSon format
			String inputLine;
			String jsonText1="";

			try {
				while ((inputLine = in.readLine()) != null){ 
					jsonText1 = jsonText1+inputLine;
					//if(!inputLine.equals(""))
					//	Log.d("Print the data from update", jsonText1);

				}
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//Log.d("Update from DB", jsonText1.trim());


			//Creates a parser to parse the data from JSon format
			JSONParser parser=new JSONParser();
			ContainerFactory containerFactory = new ContainerFactory(){
				public List creatArrayContainer() {
					return new LinkedList();
				}

				public Map createObjectContainer() {
					return new LinkedHashMap();
				}

			};
			//Maps the each key to values in each line iteratively          

			Map json = null;
			try {
				json = (Map)parser.parse(jsonText1, containerFactory);
			} catch (ParseException e) {

				e.printStackTrace();
			}

			Iterator iter = json.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry entry = (Map.Entry)iter.next();
				//Log.d("parsed key-values", entry.getValue().toString()); 

				///////////////// We will get the document id to read the document for workAssignmentID@workRequestID
				if (entry.getKey().toString().equals("results")){

					String jsonText2 = null;

					jsonText2 = entry.getValue().toString();
					Log.d("jsonText2", jsonText2);
					int firstindx = jsonText2.indexOf("id=");
					int secondindx = jsonText2.indexOf("changes=");
					doc_id = jsonText2.substring(firstindx +3 , secondindx - 2);
					//jsonSeq = Integer.parseInt(jsonText2.substring(jsonText2.indexOf("seq=")+ 4, firstindx - 2));
					//Log.d("today's solution", jsonText2);

					con.disconnect(); // I close the connection here, but it might be closed under some other circumctances.
				} // end of if(entry.getKey().toString().equals("results")) 
			} //end of while

			//////////////// Now we will read the document for workEstimateStatus changes


			URL url1 = null;

			try {
				url1 = new URL("http://hpc.iriscouch.com:5984/brokerdb/" +doc_id);


			} catch (MalformedURLException e) {

				e.printStackTrace();
			}
			HttpURLConnection urlConnection1 = null;
			try {
				urlConnection1 = (HttpURLConnection) url1.openConnection();
			} catch (IOException e) {

				e.printStackTrace();
			}
			//   InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader in12 = null;
			try {
				in12 = new BufferedReader(
						new InputStreamReader(
								urlConnection1.getInputStream()));
			} catch (IOException e) {

				e.printStackTrace();
			}
			String inputLine12;
			String json1="";
			try {
				while ((inputLine12 = in12.readLine()) != null){ 
					json1 = json1+inputLine12;
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			try {
				in12.close();
			} catch (IOException e) {

				e.printStackTrace();
			}

			Log.d("Json", json1);	

			JSONObject jObject = null;
			try {
				jObject = new JSONObject(json1);
			} catch (JSONException e1) {

				e1.printStackTrace();
			}

			synchronized(lock1){
			try {

				workEstimateStatus = jObject.getString("estimateStatus");
				statusflag=1;
				
				workAssignmentID = jObject.getString("workAssignmentID");
				workInputID = jObject.getString("workInputID");
				//workOutputID = jObject.getString("workOutputID");
				workRequestID = jObject.getString("workRequestID");
				Log.d("workAssignment", workAssignmentID);
				Log.d("workInput", workInputID);
				
				Log.d("workEstimateStatus", workEstimateStatus);


			} catch (JSONException e1) {
				
				
				statusflag=0;
				
		
				Log.d("no workEstimateStatus", "not found");
				e1.printStackTrace();
			}
			
			} //end of synchronized
			
		
				}//end of override run method
				
				};
				
				Thread T1 = new Thread(runnable1);
				T1.start();
				try {
					T1.join();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			
			synchronized(lock2){
			if(statusflag==1){

				if (workEstimateStatus.equals("approved")){
					//do something to change the UI to GoogleMap
					Log.d("the status is", "approved");
					flag = 1;


					//Starting a new Intent
					Intent nextScreen = new Intent(getApplicationContext(), GoogleMap.class);
					Log.d("where r u?", "im inside Intent");
					//Sending data to another Activity
					nextScreen.putExtra("doc_id", doc_id);
					nextScreen.putExtra("jsonSeq", Integer.toString(jsonSeq));
					//Log.d("sequence is being sent", Integer.toString(jsonSeq));
					nextScreen.putExtra("workInputID", workInputID);
					nextScreen.putExtra("workAssignmentID",workAssignmentID );
					nextScreen.putExtra("timeinput", timeInput);
					Log.d("timeinput", timeInput);
					startActivity(nextScreen);

				}

				if (workEstimateStatus.equals("rejected")){

					//do something to change the UI to GoogleMap
					//Log.d("the status is", "rejected");
					flag = -1;
					title.setText("Your Offer is Rejected");
				}
			} // end of if(statusflag=1)
			}
			Log.d("the status is", "waiting");

		}//end of while (flag == 0)
		}// end of else statement ALERTBOX
		/////////
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_first_ui, menu);
		return true;
	}
private boolean isNumber(String timeInput){
	try{
		Long.parseLong(timeInput);
		return true;
	}catch(Exception e){
		return false;
	}
}


}
