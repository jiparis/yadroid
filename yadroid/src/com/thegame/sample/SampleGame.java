package com.thegame.sample;

import java.util.ArrayList;
import java.util.List;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.Point;
import com.thegame.engine.Game;
import com.thegame.engine.GameObject;

public class SampleGame implements Game{

	Point mapCenter;
	int zoomLevel = 17;

	String name;
	String description;

	// Text (url?)
	String help;

	MapView mv;
	
	List<GameObject> objects = new ArrayList<GameObject>();
	
	public SampleGame(){
		objects.add(new SampleObject(new Point(36149787, -95992198),-10, 0));
		objects.add(new SampleObject(new Point(36149700, -95993348),10, 0));
		objects.add(new SampleObject(new Point(36149887, -95993000),5, 0));
		mapCenter = new Point(36149700, -95993348);
	}
	
    public void setView( MapView mv ) {
    	this.mv = mv;
    }
    
	public Point getMapCenter() {
		return mapCenter;
	}

	public void setMapCenter(Point mapCenter) {
		this.mapCenter = mapCenter;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public List<GameObject> getObjects() {
		return objects;
	}

	public void setObjects(List<GameObject> objects) {
		this.objects = objects;
	}

	static int INC_LATITUDE = 100;
	static int INC_LONGITUDE = 100;

	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();

		if ( keyCode == KeyEvent.KEYCODE_DPAD_DOWN )
    	{
        	Point pos = mv.getMapCenter();
    		mv.getController().centerMapTo(new Point( pos.getLatitudeE6()+INC_LATITUDE, pos.getLongitudeE6() ), true);
    		
    	}
    	else if ( keyCode == KeyEvent.KEYCODE_DPAD_UP )
    	{
        	Point pos = mv.getMapCenter();
    		mv.getController().centerMapTo(new Point( pos.getLatitudeE6()-INC_LATITUDE, pos.getLongitudeE6() ), true);
    	}
    	else if ( keyCode == KeyEvent.KEYCODE_DPAD_LEFT )
    	{
        	Point pos = mv.getMapCenter();
    		mv.getController().centerMapTo(new Point( pos.getLatitudeE6(), pos.getLongitudeE6()+INC_LONGITUDE ), true);
    	}
    	else if ( keyCode == KeyEvent.KEYCODE_DPAD_RIGHT )
    	{
        	Point pos = mv.getMapCenter();
    		mv.getController().centerMapTo(new Point( pos.getLatitudeE6(), pos.getLongitudeE6()-INC_LONGITUDE ), true);
    	}
    	else if ( keyCode == KeyEvent.KEYCODE_1 )
    	{
    		mv.getController().zoomTo( mv.getZoomLevel()-1 );
        	msgBox( mv.getZoomLevel() + "" );
    	}
    	else if ( keyCode == KeyEvent.KEYCODE_2 )
    	{
    		mv.getController().zoomTo( mv.getZoomLevel()+1 );
        	msgBox( mv.getZoomLevel() + "" );
    	} else
    		return false;
		return true;
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) {
		return false;
	}
	
	public boolean dispatchTrackballEvent(MotionEvent event) {
		return false;
	}

	public void msgBox( String text )
    {
		Toast toast;
        toast = Toast.makeText(mv.getContext(), text,
                Toast.LENGTH_LONG);
        toast.show();
    }

}
