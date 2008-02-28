package com.thegame.engine;

import android.graphics.Canvas;

import com.google.android.maps.Point;
import com.google.android.maps.Overlay.PixelCalculator;

/**
 * Basic Interface for objects
 * @author Jose
 *
 */
public interface GameObject {
	
	/** 
	 * draw phase in object's lifecycle
	 * @param canvas the {@link Canvas} object to draw in
	 * @param pixelCalculator a {@link PixelCalculator} object
	 */
	public void draw(Canvas canvas, PixelCalculator pixelCalculator);
	
	/**
	 * move phase in object's lifecycle
	 */
	public void move();
	
	/**
	 * get object coordinates
	 * @return
	 */
	public Point getXY();
	
	/**
	 * the engine calls this method when this object hits another
	 * @param o the other object
	 */
	public void hit(GameObject o);
	
	//these are used for collision detection. all measures are in microdegrees
	/*public int getMarginX();	    
	public int getMarginY();*/
	/**
	 * gets width in microdegrees
	 */
	public int getWidth();
	
	/**
	 * gets height in microdegrees
	 * @return
	 */
	public int getHeight();
}
