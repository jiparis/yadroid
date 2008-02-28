package com.thegame.engine;

import java.util.List;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.maps.MapView;
import com.google.android.maps.Point;

public interface Game {
    public void setView( MapView mv );
	public Point getMapCenter();
	public int getZoomLevel();
	public String getName();
	public String getDescription();
	public String getHelp();
	public List<GameObject> getObjects();
	public boolean dispatchKeyEvent(KeyEvent event);
	public boolean dispatchTouchEvent(MotionEvent event);
	public boolean dispatchTrackballEvent(MotionEvent event);
}
