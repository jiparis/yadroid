package org.jiagjl;

import org.jiagjl.controls.TimeSeekBar;
import org.jiagjl.test.TestTimeActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ShadowsActivity extends Activity {
    
	static final private int MENU_NOW_ID = Menu.FIRST;
    static final private int MENU_GOTO_ID = Menu.FIRST + 1;
    static final private int MENU_CONFIG_ID = Menu.FIRST + 2;	
	
    ShadowsView sv;
	/**
	 * EVENTS
	 */
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        sv = new ShadowsView(this);
        setContentView(sv);
    }
    
    // activity stopped and restarted
    @Override
    protected void onRestart(){
    	super.onRestart();
    }
    
	@Override
	protected void onStart() {
		super.onStart();
	}

	// Visible
	@Override
	protected void onResume() {		
		super.onResume();
		sv.registerSensors();
	}

	// focus lost
	@Override
	protected void onPause() {
		super.onPause();
		sv.unregisterSensors();
	}
	
	// invisible
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	
	// back button or destroyed by system
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * MENUS
	 */
	
	/**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test, menu);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_slider_test:
        	startActivity( new Intent( getApplicationContext(), TestTimeActivity.class ) );
            return true;
        case R.id.menu_shadow:
            startActivity( new Intent( getApplicationContext(), ShadowsActivity.class ) );
            return true;
        case R.id.menu_now:
        case R.id.menu_goto:
        case R.id.menu_config:
        	return true;        
        }
        return false;
    }    
}