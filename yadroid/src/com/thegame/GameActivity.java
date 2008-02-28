package com.thegame;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.thegame.engine.Engine;
import com.thegame.engine.Game;
import com.thegame.sample.SampleGame;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GameActivity extends MapActivity {
	
	public MapView mv = null;
	Engine engine;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mv = new MapView(this);
		
        //aquí se crearía una instancia del juego seleccionado por el usuario
        Game game = new SampleGame();
        game.setView(mv);

        setContentView(mv);       

        //initiates engine
        this.engine = new Engine(game, mv, 1);
        this.engine.init();
    }
    
	//"Keyboard provider"
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ( !this.engine.dispatchKeyEvent(event) )
			return super.dispatchKeyEvent(event);
		return true;
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) {
		if ( !this.engine.dispatchTouchEvent(event) )
			return super.dispatchTouchEvent(event);
		return true;
	}
	
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if ( !this.engine.dispatchTrackballEvent(event) )
			return super.dispatchTrackballEvent(event);
		return true;
	}

}