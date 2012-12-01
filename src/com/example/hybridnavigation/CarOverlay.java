package com.example.hybridnavigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class CarOverlay extends Overlay 

{

	GeoPoint geoPoint = null;
	GeoPoint geoPoint1 = null;
	int flag;

	public CarOverlay(GeoPoint g1 /*, GeoPoint g2*/){
		geoPoint = g1;
		//geoPoint1= g2;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		    Projection projection = mapView.getProjection();
			Point myPoint = new Point();
			projection.toPixels(geoPoint, myPoint);
			Bitmap marker =null;
		    marker = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.car);
			canvas.drawBitmap(marker, myPoint.x - 10, myPoint.y - 10, null);
			
			        
		}

	}

