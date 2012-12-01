package com.example.hybridnavigation;

//import java.util.ArrayList;
import java.net.*;
import org.xml.sax.InputSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.*;

import javax.xml.xpath.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.hybridnavigation.CarOverlay;
import com.example.hybridnavigation.R;
import com.example.hybridnavigation.DemoOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
//import android.view.View.OnClickListener;
import android.widget.Toast;
//import com.google.android.maps.MapView.LayoutParams;
import android.widget.ToggleButton;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ParseException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import com.google.android.maps.MapView.LayoutParams;
import android.view.View;
import android.widget.LinearLayout;

public class GoogleMap extends MapActivity  {


	private MapView mapView = null;
	private MapController mc = null;
	private View zoomView   = null;
	private LinearLayout zoomLayout = null;
	private List<Overlay> overlays = null;
	private MapOverlay myOverlay =null;
	private GeoPoint geopoint = null;
	private int sequence = 420;
	//private String myID = "1";
	private String myID = null; 
	private int jobflag = 0;
	private int markflag= 0;
	String jsonSeq=null ;
	private String intermediate_Lat=null;
	private String intermediate_Long=null;
	private Task T=null;
	private Result rs=null;
	private String doc_id=null;
	private String workAssignmentID=null;
	private String workInputID=null;
	private String workOutputID=null;
	private String workRequestID=null;
	private URLConnDB connDB = null;
    // public ArrayList<Integer> markerpoints;
	String timeInput = null;
	JSONObject jObject = null; //jObject = json of my workAssignment document in brokerdb
	private Button btnClean = null;
	private Button btnDelete = null;
	//////for the clock
	private MalibuCountDownTimer countDownTimer;
    private long timeElapsed;
    private boolean timerHasStarted = false;
    private TextView text;
    private TextView timeElapsedView;
    //private final long startTime = 50000;
    private long startTime ;
    private final long interval = 1000;

    ArrayList<Integer> markerpoints= new ArrayList<Integer>();
    //new variables for complete animation
    private Handler uihandler = new Handler();
	List<Overlay> lay= null;
	MapView mapv = null;
	//
	String duration = null;
	Long playerduration = 0L;
	private TextView actualtimer = null;
	private TextView playertime = null;
	public List<GeoPoint> pairs;
	public GeoPoint p;
	private String xml="";




	// This block, MapOverlay, gets the GeoPoints of touched screen pixel!
	class MapOverlay extends com.google.android.maps.Overlay {




		@Override
		public boolean onTouchEvent(MotionEvent event, final MapView mapView) {

			// when worker lifts his finger
			if (event.getAction() ==1) {
				//put a flag to run it when Mark Toggle is on
				if(markflag==1){
					p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
					intermediate_Lat = Double.toString(p.getLatitudeE6() / 1E6) ;
					intermediate_Long = Double.toString(p.getLongitudeE6() / 1E6);
					Log.d("intermediate_Lat", intermediate_Lat);
					Log.d("intermediate_Long", intermediate_Long);
					
					btnDelete.setEnabled(true);
					btnClean.setEnabled(true);


					pairs = null;

					pairs = getDirectionData(T.getSource_Latitude(), T.getSource_Longitude() ,intermediate_Lat, intermediate_Long);
					playerduration = playerduration + Long.parseLong(duration);
					SimpleDateFormat df1 = new SimpleDateFormat("mm:ss");
				    String formattedDate1 = df1.format(new Date(playerduration*1000));
	                playertime.setText("Time:" + formattedDate1);
					
					if(pairs != null){
						//Log.d("points from DB", pairs.get(0).toString());
 						rs.lifo.push(pairs.size() - 1);
						//push all the midpoint to DB
						for(GeoPoint s : pairs){
							//Log.d("arraylist", s.toString());
							rs.resultlist.add(s.toString());
						}

						int rsize= rs.resultlist.size();
						rs.resultlist.remove(rsize -1);
						Log.d("resultlist string", rs.resultlist.toString());
						Log.d("the directionsteps", rs.directionsteps.toString());

						//We have the nodes for the work result, and put them into JSon Object
						String[] lngLat = pairs.get(0).toString().split(",");
						Log.d("lngLat", lngLat[1]);
						Log.d( "lngLat", lngLat[0]);

						GeoPoint startGP = pairs.get(0);// new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6), (int) (Double.parseDouble(lngLat[1]) * 1E6));
						geopoint = startGP;

						GeoPoint gp1;
						GeoPoint gp2 = startGP;
						
						if(mapView.getOverlays().size() > 5)
						mapView.getOverlays().remove(mapView.getOverlays().size()-1);
						for (int i = 1; i < pairs.size(); i++) {
							lngLat = pairs.get(i).toString().split(",");
							gp1 = gp2;
							gp2 = pairs.get(i);//new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6),(int) (Double.parseDouble(lngLat[1]) * 1E6));
							mapView.getOverlays().add(new DemoOverlay(gp1, gp2, 3));
							
							//Log.d("xxx", "pair:" + pairs.get(i).toString());
						}

						//END POINT
						mapView.getOverlays().add(new DemoOverlay(gp2, gp2, 2 ));
						markerpoints.add(rs.resultlist.size() -1 );
						lay = mapView.getOverlays();
						lay.add(new CarOverlay(pairs.get(0)));
						mapv= mapView;
						markflag=0;
						((ToggleButton) findViewById(R.id.togglemarkzoom)).setChecked(false);
						Runnable runnable = new Runnable() {
							 
						      public void run() {
						    	  for(int i=1; i < pairs.size(); i++){
						          final int value = i;
						          try {
						            Thread.sleep(80);
						          } catch (InterruptedException e) {
						            e.printStackTrace();
						          }
						          uihandler.post(new Runnable() {
						            public void run() {
						            	lay.remove(lay.size()-1);
						            	lay.add(new CarOverlay(pairs.get(value)));
						            	mapView.postInvalidate();
						            }
						          });
						        } //end of for loop
						    	  
						      }
						    };
						    
						    new Thread(runnable).start();
						    
						

						lngLat = pairs.get(pairs.size() - 1).toString().split(","); //split(",");
						T.setSource_Latitude(intermediate_Lat);
						T.setSource_Longitude(intermediate_Long);
						
					}
					
					 


					Toast.makeText(
							getBaseContext(),
							p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6()
							/ 1E6, Toast.LENGTH_SHORT).show(); 
				
				}
			}
			return false;

		}




}
	public void onToggleJobSendClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();


		if (on) {
			// Enable vibrate
			//Log.d("ToggleJobSend", "on");

			
			//   if (jobflag==1){
			Runnable runnable = new Runnable(){
			
			public void run(){			         
			connDB = new URLConnDB("brokerdb", doc_id);
			jObject = connDB.getJson();


			///////////////////// Now go to listen on workInputID and get the id to pass it to Task(id)    
			HttpURLConnection connect1 = null;


			try {
				connect1 = (HttpURLConnection) new URL("http://hpc.iriscouch.com/iodb/_design/WorkInput/_view/by_workInputID?key=\""+workInputID+"\"").openConnection();
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
				
				buffreader.close();
				
			} catch (IOException e1) {

				e1.printStackTrace();
			}


			JSONObject jObject1=null;
			try {
				jObject1= new JSONObject(json_Text1);
			 	json_Text1 = jObject1.getString("rows");
				//Log.d("rows", json_Text1);
			} catch (JSONException e2) {

				e2.printStackTrace();
			}
			
			int firstindx = json_Text1.indexOf("value\":");
			int secondindx = json_Text1.indexOf("id\":");
			json_Text1 = json_Text1.substring(firstindx +8 , secondindx - 3);
			//Log.d("surprise", json_Text1);
			myID = json_Text1;

     	}//end of overriderun()
			};
			
			Thread getjob_thread = new Thread(runnable);
			getjob_thread.start();
			try {
				getjob_thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//clear the overlay before getting the new job
			mapView.getOverlays().clear();
			mapView.invalidate();
			myOverlay = new MapOverlay();

			overlays = mapView.getOverlays();
			overlays.add(myOverlay);

			//if(jobflag==1){
			T = new Task(myID);
			workOutputID = UUID.randomUUID().toString();  
			rs = new Result(/*doc_output_id, */"iodb", workAssignmentID,workOutputID);
			//rs = new Result(myID,"iodb");


			//setting the start and end points with marker
			GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(T.getSource_Latitude()) * 1E6), (int) (Double.parseDouble(T.getSource_Longitude()) * 1E6));
			GeoPoint endGP = new GeoPoint((int) (Double.parseDouble(T.getDest_Latitude()) * 1E6), (int) (Double.parseDouble(T.getDest_Longitude()) * 1E6));
			mc.setCenter(startGP);
			mc.setZoom(10);
			mapView.getOverlays().add(new DemoOverlay(startGP, startGP, 0));
			mapView.getOverlays().add(new DemoOverlay(endGP, endGP, 1));
			
			getDirectionData(T.originalSourceLat, T.originalSourceLong ,T.originalDestinationLat, T.originalDestinationLong);
		    Log.d("original duration", duration);
		    actualtimer = (TextView) this.findViewById(R.id.actualtimer);
		    SimpleDateFormat df = new SimpleDateFormat("mm:ss");
		    String formattedDate0 = df.format(new Date(Long.parseLong(duration)*1000));
            actualtimer.setText("Time:" + formattedDate0);
            
            playertime = (TextView) this.findViewById(R.id.playertime);
		    SimpleDateFormat df1 = new SimpleDateFormat("mm:ss");
		    
		    String formattedDate1= df1.format(new Date(0));
            playertime.setText("Time:" + formattedDate1);
			
			
			////////start the clock here!
			text = (TextView) this.findViewById(R.id.timer);
	        timeElapsedView = (TextView) this.findViewById(R.id.timeElapsed);
	        countDownTimer = new MalibuCountDownTimer(startTime, interval);
	        text.setText(text.getText() + String.valueOf(startTime));
	        
	        if (!timerHasStarted)
            {
                countDownTimer.start();
                timerHasStarted = true;
            }
               else
            {
                countDownTimer.cancel();
                timerHasStarted = false;              
            }


			
			//////end of the clock code



		} //end of ToggleJobSend on

		else {
			// Disable vibrate
			Log.d("ToggleJobSend", "off");

			if (intermediate_Lat==null || intermediate_Long==null){
				intermediate_Lat = T.getSource_Latitude();
				intermediate_Long = T.getSource_Longitude();
			}

			pairs = getDirectionData(intermediate_Lat, intermediate_Long,
					T.getDest_Latitude(), T.getDest_Longitude());

			if (pairs != null){
				//Log.d("points from DB", pairs[0]);
				//getting the results into StringArray
				for(GeoPoint s : pairs){
					//Log.d("arraylistofsendjob", s);
					rs.resultlist.add(s.toString());
				}
				rs.hashMapdirectionsteps.put("results", rs.directionsteps.toString());
				Log.d("the directionsteps", rs.directionsteps.toString());
				rs.insertIntoDb();
				//updating the Assignment doc with status
				try {
					jObject.put("status", "completed");
					jObject.put("workOutputID", workOutputID );
					jObject.put("assignmentStatus", "outputgiven" );
				} catch (JSONException e) {

					e.printStackTrace();
				} 
				Log.d("WorkOutputID", jObject.toString());
				
				Runnable runnable = new Runnable() {
				public void run(){
				HttpURLConnection con2 = null;
				try {
					con2 = (HttpURLConnection) new URL("http://hpc.iriscouch.com:5984/brokerdb").openConnection();
				} catch (MalformedURLException e2) {

					e2.printStackTrace();
				} catch (IOException e2) {

					e2.printStackTrace();
				}
				String encoded = Base64.encodeToString("joe:hpc1234".getBytes(), Base64.NO_WRAP); 
				con2.setRequestProperty("Authorization", "Basic "+encoded);
				 
				con2.setDoOutput(true);
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
					os.write(jObject.toString().getBytes());
					os.flush();
				} catch (IOException e) {

					e.printStackTrace();
				}
				try {
					System.out.println(con2.getInputStream().toString());
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				con2.disconnect();
				// end of update

				} // end of run();
				};
				
				Thread jobsend_thread = new Thread(runnable);
				jobsend_thread.start();
				try {
					jobsend_thread.join();
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
				
				//We have the nodes for the work result, and put them into JSon Object
				String[] lngLat = pairs.get(0).toString().split(",");
				Log.d("in send button", lngLat[1]);
				GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[0])), (int) (Double.parseDouble(lngLat[1])));
				geopoint = startGP;


				GeoPoint gp1;
				GeoPoint gp2 = startGP;

				//for (int i = 1; i < pairs.size(); i++) {
				//lngLat = pairs.get(i).toString().split(",");
				//gp1 = gp2;
				//gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6),(int) (Double.parseDouble(lngLat[1]) * 1E6));
				//mapView.getOverlays().add(new DemoOverlay(gp1, gp2, 3));
				//Log.d("xxx", "pair:" + pairs[i]);
				//}
				// END POINT
				//mapView.getOverlays().add(new DemoOverlay(gp2, gp2, 3));
				//this line is again animating to startPoint
				//mapView.getController().animateTo(startGP);
				
				
				if(mapView.getOverlays().size() > 5)
					mapView.getOverlays().remove(mapView.getOverlays().size()-1);
					for (int i = 1; i < pairs.size(); i++) {
						lngLat = pairs.get(i).toString().split(",");
						gp1 = gp2;
						gp2 = pairs.get(i);//new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6),(int) (Double.parseDouble(lngLat[1]) * 1E6));
						mapView.getOverlays().add(new DemoOverlay(gp1, gp2, 3));
						
						Log.d("xxx", "pair:" + pairs.get(i).toString());
					}

					//END POINT
					mapView.getOverlays().add(new DemoOverlay(gp2, gp2, 3 ));
					lay = mapView.getOverlays();
					lay.add(new CarOverlay(pairs.get(0)));
					mapv= mapView;
					markflag=0;
					((ToggleButton) findViewById(R.id.togglemarkzoom)).setChecked(false);
					Runnable runnable2 = new Runnable() {
						 
					      public void run() {
					    	  for(int i=1; i < pairs.size(); i++){
					          final int value = i;
					          try {
					            Thread.sleep(80);
					          } catch (InterruptedException e) {
					            e.printStackTrace();
					          }
					          uihandler.post(new Runnable() {
					            public void run() {
					            	lay.remove(lay.size()-1);
					            	lay.add(new CarOverlay(pairs.get(value)));
					            	mapView.postInvalidate();
					            }
					          });
					        } //end of for loop
					    	  //uihandler.post(new Runnable() {
						           //public void run() {
						             //markflag = 1;	
								     //((ToggleButton) findViewById(R.id.togglemarkzoom)).setChecked(true);
						             //mapView.postInvalidate();
						           //}
						          //});
					    	  
					    	  
					    	  
					      }
					    };
					    
					    new Thread(runnable2).start();

				
				
			}
			//jobflag = 0;
			//This returns the ui back to FirstUI
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(15000);
						finish();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();


		}
	}
	
	//////the subclass that I override for the Clock
	public class MalibuCountDownTimer extends CountDownTimer
    {
        public MalibuCountDownTimer(long startTime, long interval)
            {
                super(startTime, interval);
            }
        @Override
        public void onFinish()
            {
                text.setText("Time's up!");
                timeElapsedView.setText("Time Elapsed: " + String.valueOf(startTime));
                
                //This returns the ui back to FirstUI
    			new Thread(new Runnable() {
    				@Override
    				public void run() {
    					try {
    						Thread.sleep(2000);
    						finish();
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
    				}
    			}).start();
            }
        
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        @Override
        public void onTick(long millisUntilFinished)
            {
        	
            String formattedDate = df.format(new Date(millisUntilFinished));
                text.setText("Time remain:" + formattedDate);
                timeElapsed = startTime - millisUntilFinished;
                timeElapsedView.setText("Time Elapsed: " + df.format(new Date(timeElapsed)));
            }
    }
	
	

	public void onToggleMarkZoomClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			// Enable vibrate
			//Log.d("Mark", "on");
			markflag=1 ;


		} else {
			// Disable vibrate
			//Log.d("Zoom", "off");
			markflag=0;
		}
	}
	
	public void onClearButtonClicked(View view){
		//when the worker wants to clean his results
		//Log.d("message", "it is working");
		//clear the overlay before getting the new job
		mapView.getOverlays().clear();
		//mapView.invalidate();
		myOverlay = new MapOverlay();
		overlays = mapView.getOverlays();
		overlays.add(myOverlay);

		
		T = new Task(myID);
		//workOutputID = UUID.randomUUID().toString();  
		//rs = new Result(/*doc_output_id, */"iodb", workAssignmentID,workOutputID);
		//setting the start and end points with marker
		GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(T.getSource_Latitude()) * 1E6), (int) (Double.parseDouble(T.getSource_Longitude()) * 1E6));
		GeoPoint endGP = new GeoPoint((int) (Double.parseDouble(T.getDest_Latitude()) * 1E6), (int) (Double.parseDouble(T.getDest_Longitude()) * 1E6));
		//mc.setCenter(startGP);
		mc.setZoom(10);
		mapView.getOverlays().add(new DemoOverlay(startGP, startGP, 0));
		mapView.getOverlays().add(new DemoOverlay(endGP, endGP, 1));
		btnClean.setEnabled(false);
		rs.clear();
		btnClean.setEnabled(false);

		}
	
	public void onDeleteButtonClicked(View view){
		 
		int x= rs.resultlist.size();
		int y = (Integer) rs.lifo.peek();
		int z = (Integer) rs.lifo.size();
		//Log.d("see the size of resultlist", String.valueOf(x));
		//Log.d("see the size of lasttouchedlist", String.valueOf(y));
		//Log.d("see the size of stack size", String.valueOf(z));
		//Log.d("see the resultlist", rs.resultlist.get(x-1));
		mapView.getOverlays().clear();
		mapView.invalidate();
		myOverlay = new MapOverlay();
		overlays = mapView.getOverlays();
		overlays.add(myOverlay);
		if (y==x){
			T = new Task(myID);
			GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(T.originalSourceLat) * 1E6) , (int) (Double.parseDouble(T.originalSourceLong) * 1E6));
			GeoPoint endGP = new GeoPoint((int) (Double.parseDouble(T.originalDestinationLat) * 1E6) , (int) (Double.parseDouble(T.originalDestinationLong) * 1E6));
		    //mc.setCenter(startGP);
			mc.setZoom(10);
			mapView.getOverlays().add(new DemoOverlay(startGP, startGP, 0));
			mapView.getOverlays().add(new DemoOverlay(endGP, endGP, 1));
			btnDelete.setEnabled(false);
			
		} else{
		
		for (int i  = 0 ; i<x-y ; i++){
			//Log.d("see the loopinside", rs.resultlist.get(i));
			String[] lngLat = rs.resultlist.get(i).toString().split(","); 
			//Log.d( "lngLat delete", lngLat[0]);
			//Log.d( "lngLat delete", lngLat[1]);
			String[] lngLat1 = rs.resultlist.get(i+1).toString().split(",");
			//Log.d( "lngLat1 delete", lngLat1[0]);
			//Log.d( "lngLat1 delete", lngLat[1]);
			
			GeoPoint gp1 = new GeoPoint((int) (Double.parseDouble(lngLat[0]) ) , (int) (Double.parseDouble(lngLat[1]) ));
			GeoPoint gp2 = new GeoPoint((int) (Double.parseDouble(lngLat1[0]) ) , (int) (Double.parseDouble(lngLat1[1]) ));
						
			//Log.d( "geopoints inside delete", String.valueOf(gp1));
			//Log.d( "geopoints inside delete", gp2.toString());
			
			GeoPoint gps = new GeoPoint((int) (Double.parseDouble(T.originalSourceLat) * 1E6) , (int) (Double.parseDouble(T.originalSourceLong) * 1E6));
			GeoPoint gpd = new GeoPoint((int) (Double.parseDouble(T.originalDestinationLat) * 1E6) , (int) (Double.parseDouble(T.originalDestinationLong) * 1E6));
			//Log.d( "original source", T.originalSourceLat);
			//Log.d( "original source", T.originalSourceLong);
			//Log.d( "geopoints delete", String.valueOf(gps));
			//Log.d( "geopoints delete", String.valueOf(gps));
			//Log.d( "geopoints delete", gpd.toString());
			
			mapView.getOverlays().add(new DemoOverlay(gps, gps, 0));
			mapView.getOverlays().add(new DemoOverlay(gpd, gpd, 1));
			mapView.getOverlays().add(new DemoOverlay(gp1, gp2, 3));
			for (int j=0; j<markerpoints.size(); j++){
				if (i+1==markerpoints.get(j)){
					Log.d("are you inside ", Integer.toString(i) + "to draw the marker");
					mapView.getOverlays().add(new DemoOverlay(gp2, gp2, 2));
				}
			}

			
			
		}
			String[] lngLat3 = rs.resultlist.get(x-y-1).split(","); 		
			GeoPoint gp1 = new GeoPoint((int) (Double.parseDouble(lngLat3[0]) ) , (int) (Double.parseDouble(lngLat3[1]) ));
				
			mapView.getController().animateTo(gp1);
		
		}
		//Log.d("markerpoints",markerpoints.toString() );
		//markerpoints.remove(markerpoints.size()-1);
		//Log.d("markerpoints",markerpoints.toString() );
		rs.delete();
		if (!rs.resultlist.isEmpty()){
		String[] lngLat2 = rs.resultlist.get(x-y-1).split(",");
		T.setSource_Latitude(lngLat2[0]);
		T.setSource_Longitude(lngLat2[1]);		
		Log.d("markerpoints",markerpoints.toString() );
		markerpoints.remove(markerpoints.size()-1);
		Log.d("markerpoints",markerpoints.toString() );
		//Log.d("sanity check", lngLat2[0]);
		//Log.d("sanity check", lngLat2[1]);
		}
		else{
			T.setSource_Latitude(T.originalSourceLat);
			T.setSource_Longitude(T.originalSourceLong);
			
		}
	
	}


	//Starts the main activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent nextScreen = getIntent();
		// Receiving the Data
		//String 
		doc_id = nextScreen.getStringExtra("doc_id");
		//String 
		jsonSeq = nextScreen.getStringExtra("jsonSeq");
		workInputID = nextScreen.getStringExtra("workInputID");
		workAssignmentID = nextScreen.getStringExtra("workAssignmentID");
		timeInput = nextScreen.getStringExtra("timeinput");
		Log.d("time in map", timeInput);
		startTime = (Long.parseLong(timeInput))*60*1000;
		Log.e("Second Screen", doc_id );

		
		
		setContentView(R.layout.activity_google_map);
		Log.d("setting view", "done");
		btnDelete = (Button)findViewById(R.id.delete);
		btnClean = (Button) findViewById(R.id.clear);
		btnDelete.setEnabled(false);
		btnClean.setEnabled(false);




		//initilize the objects for Zoom
		//shows the google map with Zoom panel
		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mc = mapView.getController();
		//mapView.setSatellite(false);
		mc.setZoom(50);
		zoomLayout = (LinearLayout)findViewById(R.id.zoom);
		zoomView = mapView.getZoomControls();
		zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);
		//////SetTrafffic
		mapView.setSatellite(true);
		mapView.setStreetView(false);
		mapView.setTraffic(true);
		
		/////

		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	public List<GeoPoint> getDirectionData(final String sourceLat, final String sourceLong, final String destinationLat, final String destinationLong) {

		
		int statusflag=1;
		final Object lock = new Object();
		Runnable runnable1 = new Runnable(){
		  public void run(){
		
		URL google = null;
		
		try {
			google = new URL("http://maps.googleapis.com/maps/api/directions/xml?origin="+sourceLat+","+sourceLong+"&destination="+destinationLat+","+destinationLong + "&sensor=false");
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
		
		synchronized(lock){
		xml = "";
		try {
			while ((inputLine = in.readLine()) != null){ 
				xml = xml+inputLine;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		}
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Log.d("Results from URL->", xml);

    	} //end of override run()
    	};
    	
    	
    	Thread getdirection_thread = new Thread(runnable1);
    	getdirection_thread.start();
    	try {
			getdirection_thread.join();
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}

		InputStream is = new ByteArrayInputStream(xml.getBytes());
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String status = null;
		InputSource inputXml = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		try {
			NodeList snl= (NodeList) xpath.evaluate("/DirectionsResponse/status", inputXml, XPathConstants.NODESET);
			status = snl.item(0).getTextContent();
		    Log.d("status", status);
			if (!status.equals("OK")){
				statusflag= 0;

			}
			
		
		NodeList nldirection  =null;
		InputSource inputxml2 = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		try{
			nldirection = (NodeList) xpath.evaluate("/DirectionsResponse/route/leg/step/html_instructions", inputxml2, XPathConstants.NODESET);
			if ( sourceLat.equals(T.originalSourceLat) && sourceLong.equals(T.originalSourceLong) && destinationLat.equals(T.originalDestinationLat) && destinationLong.equals(T.originalDestinationLong))
			{}else{
			for(int s = 0; s< nldirection.getLength(); s++){
				String nodeString =	nldirection.item(s).getTextContent();
				rs.directionsteps.add(nodeString);
				Log.d("turn by turn instruction", nodeString);
			}
			}
			
		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		
		//end of if

		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		String pathConent = "";
		List<GeoPoint> gpl = new ArrayList<GeoPoint>();
		if(statusflag==1){
			
			//InputStream is = new ByteArrayInputStream(xml.getBytes());
			XPathFactory factory1 = XPathFactory.newInstance();
			XPath xpath1 = factory1.newXPath();
			//String duration = null;
			InputSource inputXml2 = new InputSource(new ByteArrayInputStream(xml.getBytes()));
			try {
				NodeList snl= (NodeList) xpath.evaluate("/DirectionsResponse/route/leg/duration/value", inputXml2, XPathConstants.NODESET);
				duration = snl.item(0).getTextContent();
				Log.d("duration", duration);
			
			/////
			NodeList nl = null;
			InputSource inputXml1 = new InputSource(is);
			//try {
				nl = (NodeList) xpath.evaluate("/DirectionsResponse/route/leg/step/polyline/points", inputXml1, XPathConstants.NODESET);
				
				for (int s = 0; s < nl.getLength(); s++) {
						
					gpl.addAll(DecodeHelper.decodePoly(nl.item(s).getTextContent()));
//					String nodeString = nl.item(s).getTextContent();
//					nodeString = nodeString.trim().replaceAll("( )+", ",");
//					pathConent = pathConent+" "+nodeString;

				}
			} catch (XPathExpressionException e) {

				e.printStackTrace();
			}
			Log.d("pathcontent",pathConent);
			Log.d("pathcontent",pathConent.trim());
			//Log.d("geopoint list", gpl.toString());
			return gpl;
		//	String[] tempContent = pathConent.trim().split(" ");
			
		//	return tempContent;
			
		//}
		}else {
			return null;
		}

	
}
}