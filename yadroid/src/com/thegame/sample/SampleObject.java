package com.thegame.sample;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.Point;
import com.google.android.maps.Overlay.PixelCalculator;
import com.thegame.engine.GameObject;

public class SampleObject implements GameObject{
	
	Point point;
	int incX;
	int incY;
	public SampleObject(Point initial, int incX, int incY){
		point = initial;
		this.incX = incX;
		this.incY = incY;
		
	    paint2.setARGB(255, 255, 255, 255);
	}
	
	
	public Point getInitialPosition(){
		return point;
	}	
	
	Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    
    int n = 0;
    //long next = SystemClock.uptimeMillis();

	public void draw(Canvas canvas, PixelCalculator pixelCalculator) {
		int[] screenCoords = new int[2];
        
        pixelCalculator.getPointXY(point, screenCoords);
        canvas.drawCircle(screenCoords[0], screenCoords[1], 9, paint1);
        canvas.drawText(Integer.toString(n),
                screenCoords[0] - 4,
                screenCoords[1] + 4, paint2);		
	}


	public void move() {
		//only updates position every 1 second
		//if(SystemClock.uptimeMillis() >= next){
        	point = new Point(point.getLatitudeE6() + incY, point.getLongitudeE6() + incX);
        	n++;
        //	next += 1000;
        //}
	}

	public Point getXY(){
		return point;
	}


	public int getHeight() {
		return 500;
	}


	public int getWidth() {
		return 500;
	}
	
	public void hit(GameObject ob){
		Log.i("SampleObject", "Collision detected: " + ob.toString());
	}

}
