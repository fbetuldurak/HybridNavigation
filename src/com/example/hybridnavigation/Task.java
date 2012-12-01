package com.example.hybridnavigation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.net.ParseException;
import android.net.Uri;
import android.util.Log;

public class Task {

	String ID = null;
	private String input = null;
	private String startLoc = null;
	private String endLoc =null;
	private String type = null;
	private String workInputID = null;
	private String Source_Latitude=null;
	private String Source_Longitude=null;
	private String Dest_Latitude=null;
	private String Dest_Longitude=null;	
	public String originalSourceLat=null;
	public String originalSourceLong=null;
	public String originalDestinationLat=null;
	public String originalDestinationLong=null;

	public Task(final String ID){
		
		this.ID = ID;
		Runnable runnable= new Runnable(){
//			private String type;
//			private String workInputID;
//			private String startLoc;
//			private String endLoc;

			public void run(){
		
		
		URL url = null;

		try {
			url = new URL("http://hpc.iriscouch.com/iodb/" +ID);


		} catch (MalformedURLException e) {

			e.printStackTrace();
		}
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {

			e.printStackTrace();
		}
		//   InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
							urlConnection.getInputStream()));
		} catch (IOException e) {

			e.printStackTrace();
		}
		String inputLine;
		String xml="";
		try {
			while ((inputLine = in.readLine()) != null){ 
				xml = xml+inputLine;
				//  System.out.println(inputLine);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

		//Log.d("Json", xml);	
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(xml);
		} catch (JSONException e1) {
			
			e1.printStackTrace();
		}

		JSONObject jinput = null;
		try {
			type = jObject.getString("type");
			workInputID = jObject.getString("workInputID");
			jinput = jObject.getJSONObject("input");
		} catch (JSONException e1) {
			
			e1.printStackTrace();
		}

		try {
			startLoc = jinput.getString("Start Location");
		} catch (JSONException e1) {
			
			e1.printStackTrace();
		}
		try {
			endLoc = jinput.getString("End Location");
		} catch (JSONException e1) {
			
			e1.printStackTrace();
		}
		//Log.d("type", this.type);
		//Log.d("workInputID", this.workInputID);
		Log.d("Original startloc", startLoc);
		Log.d("Original endloc",endLoc);
		findAndSetGeocode(startLoc,"Source");
		findAndSetGeocode(endLoc,"Dest");

		////				else if (entry.getKey().equals("Source_Latitude") ){
		////					this.Source_Latitude = entry.getValue().toString();
		////				}
		////				else if(entry.getKey().equals("Source_Longitude")){
		////					this.Source_Longitude= entry.getValue().toString();
		////				}
		////				else if(entry.getKey().equals("Dest_Latitude")){
		////					this.Dest_Latitude = entry.getValue().toString();
		////				}
		////				else if(entry.getKey().equals("Dest_Longitude")){
		////					this.Dest_Longitude = entry.getValue().toString();
		////				}
		//			}
		//
		//			//Log.d("JSonValues", JSONValue.toString(json)   

		}
		};
		
		Thread task_thread = new Thread(runnable);
		task_thread.start();
		try {
			task_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	private void findAndSetGeocode( String addr,  String ODtype){
		
		
		//Runnable runnable1 = new Runnable(){
			
			//public void run(){
		Log.d("In findAndSetGeocode", addr);
		URL google = null;
		int statusflag=1;
		try {
			google = new URL("http://maps.googleapis.com/maps/api/geocode/xml?address="+Uri.encode(addr)+"&sensor=false");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		URLConnection yc = null;
		try {
			yc = google.openConnection();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader( yc.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String inputLine;
		String xml="";
		try {
			while ((inputLine = in.readLine()) != null){ 
				xml = xml+inputLine;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Log.d("Results from URL->", xml);

		InputStream is = new ByteArrayInputStream(xml.getBytes());
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String status = null;
		InputSource inputXml = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		InputSource inputXml1 = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		InputSource inputXml2 = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		try {
			NodeList snl= (NodeList) xpath.evaluate("/GeocodeResponse/status", inputXml, XPathConstants.NODESET);
			status = snl.item(0).getTextContent();
			Log.d("status", status);
			if (!status.equals("OK")){
				statusflag= 0;

			}


		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		String lat =null;
		String lng = null;
		try {
			NodeList snl= (NodeList) xpath.evaluate("/GeocodeResponse/result/geometry/location/lat", inputXml1, XPathConstants.NODESET);
			lat = snl.item(0).getTextContent();
			//lng = snl.item(0).getLastChild().getTextContent();
			Log.d("lat", lat);
			//Log.d("lng", lng);

		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		try {
			NodeList snl= (NodeList) xpath.evaluate("/GeocodeResponse/result/geometry/location/lng", inputXml2, XPathConstants.NODESET);
			//lat = snl.item(0).getTextContent();
			lng = snl.item(0).getTextContent();
			Log.d("lat", lat);
			Log.d("lng", lng);

		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		if(ODtype.equals("Source")){
			this.setSource_Latitude(lat);
			this.setSource_Longitude(lng);
			this.originalSourceLat=lat;
			this.originalSourceLong=lng;
		}else if(ODtype.equals("Dest")){
			this.setDest_Latitude(lat);
			this.setDest_Longitude(lng);
			this.originalDestinationLat=lat;
			this.originalDestinationLong=lng;
		}

		//}//end of overriderun()
	//}; //end of thread

	}

	public void setSource_Latitude(String Source_Latitude){
		this.Source_Latitude = Source_Latitude;
	}

	public String getSource_Latitude(){
		return this.Source_Latitude;
	}

	public void setSource_Longitude(String Source_Longitude){
		this.Source_Longitude = Source_Longitude;
	}

	public String getSource_Longitude(){
		return this.Source_Longitude;
	}

	public void setDest_Latitude(String Dest_Latitude){
		this.Dest_Latitude = Dest_Latitude;
	}

	public String getDest_Latitude(){
		return this.Dest_Latitude;
	}

	public void setDest_Longitude(String Dest_Longitude){
		this.Dest_Longitude = Dest_Longitude;
	}

	public String getDest_Longitude(){
		return this.Dest_Longitude;	
	}

}
