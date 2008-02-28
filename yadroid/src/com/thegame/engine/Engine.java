package com.thegame.engine;

import java.util.List;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayController;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Engine {

	MapView mv;
	int fps = 20;
	Game game;
	public OverlayController oc;
	Thread engineThread;
	
	public Engine(Game game, MapView mv, int fps){
		this.mv = mv;
		this.fps = fps;
		this.game = game;
		
		oc = mv.createOverlayController();
		oc.add(new BasicOverlay(game), true);
	}
	
	public void init(){
		mv.getController().centerMapTo(game.getMapCenter(), false);
		mv.getController().zoomTo( game.getZoomLevel() );
		engineThread = new Thread(loopThread);
		engineThread.start();
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		return game.dispatchKeyEvent( event );
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) {
		return game.dispatchTouchEvent(event);
	}
	
	public boolean dispatchTrackballEvent(MotionEvent event) {
		return game.dispatchTrackballEvent(event);
	}
	
    private Runnable loopThread = new Runnable() {
		int skippedFrames = 0;
    	public void run() {			
			long frameLenght = 1000 / fps;
			long frameEnd = SystemClock.uptimeMillis() + frameLenght;
			while (true){
				long now = SystemClock.uptimeMillis();
				
				// do object lifecycle
				moveObjects();
				checkCollisions();
				// frameskip
				if(now < frameEnd - frameLenght/2){					
					// repaint 
					mv.postInvalidate();
					try {
						if(now + 3 < frameEnd)
							Thread.sleep(frameEnd - now);
						else
							Thread.yield();
					} catch (InterruptedException e) {
						Log.e("Engine", "Main thread terminated", e);
					}
					frameEnd += frameLenght;
				}
				else{					
					if(++skippedFrames > 3){
						// repaint
						mv.postInvalidate();
						frameEnd = now + frameLenght;
						skippedFrames = 0;
					}
					else{
						frameEnd += frameLenght;
					}
					Thread.yield();
				}
			}
		}
	};
	
	
	protected void moveObjects(){
		for(int i = 0; i< game.getObjects().size(); i++){
        	GameObject ob = game.getObjects().get(i);
        	ob.move();
		}
		
	}
	
	protected void checkCollisions(){
		List<GameObject> objects = game.getObjects();
		
		for (int i = 0; i < objects.size(); i++) {
			GameObject src = objects.get(i);
			if (src.getWidth() < 0) continue;
			for (int j = 0; j < objects.size(); j++) {
				GameObject dst = objects.get(j);
				if (src == dst) continue;
				if (dst.getWidth() < 0) continue;
				// Se utiliza rectangleHit, pero debería ser dependiente del objeto en cuestión (src)
				if (CollisionUtils.rectangleHit(src, dst)) {
					dst.hit(src);					
				}
			}
		}
		
	}
	
	/**
	 * Simple overlay used for drawing objects
	 * @author Jose
	 *
	 */
	class BasicOverlay extends Overlay {

	    int n = 0;
		Game game;

	    public BasicOverlay(Game gm) {        
	        this.game = gm;
	    }
	    
	    public void draw(Canvas canvas, PixelCalculator pixelCalculator, boolean b) {
	        super.draw(canvas, pixelCalculator, b);        
	        for(int i = 0; i< game.getObjects().size(); i++){
	        	GameObject ob = game.getObjects().get(i);
	        	ob.draw(canvas, pixelCalculator);
	        }        
	    }
	}
}
