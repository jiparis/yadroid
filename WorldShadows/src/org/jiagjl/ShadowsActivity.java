package org.jiagjl;

import org.jiagjl.controls.TimeSeekBar;
import org.jiagjl.test.TestTimeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShadowsActivity extends Activity {
	
    ShadowsView sv;
    
	/**
	 * EVENTS
	 */
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        
        //setContentView(sv);
        //setContentView(R.layout.main);
        //sv = (ShadowsView) findViewById(R.id.shadows_view);
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        												LayoutParams.WRAP_CONTENT));
        TextView tv = new TextView(getApplicationContext()); 
        tv.setText("textview!!");
//        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT));
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT, 1));
        ll.addView(tv);        

        sv = new ShadowsView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        												LayoutParams.FILL_PARENT, 1));
        sv.setClickable(true);
        
        TimeSeekBar timeSeekBar = new TimeSeekBar(getApplicationContext(), 8f,
				22f, 30, 10.5f, 18f, new TimeSeekBar.ITimeBarCallback() {
					public void onEndTimeValueChange(int hour, int minute) {
						Log.i( "SHADOWS", "End: " + hour + ":" + minute  );
					}

					public void onStartTimeValueChange(int hour, int minute) {
						Log.i( "SHADOWS", "Start: " + hour + ":" + minute  );
					}
				}, false);
        timeSeekBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				80, 0));
        
        ll.addView(timeSeekBar);
        
        setContentView(sv);

        addContentView(ll, new LayoutParams
        		(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
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
        case R.id.menu_now:
        case R.id.menu_goto:
        case R.id.menu_config:
        	return true;        
        }
        return false;
    }    
}