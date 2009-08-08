package org.jiagjl;

import java.util.Calendar;

import org.jiagjl.controls.TimeSeekBar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShadowsActivity extends Activity implements DatePickerDialog.OnDateSetListener {
	
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
        
        Calendar c = sv.si.getCalendar();
        float max_time = 22f;
        float min_time = 8f;
        float start_time = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0f;
        if ( start_time < min_time )
        	start_time = min_time;
        float end_time = start_time + (float)sv.si.getValue(SolarInformation.TIME_WINDOW_VALUE) / 60.0f;
        if ( end_time > max_time ){
        	end_time = max_time;
        	start_time = end_time - 1.0f;
        }
        
        TimeSeekBar timeSeekBar = new TimeSeekBar(getApplicationContext(), min_time,
        		max_time, 30, start_time, end_time, new TimeSeekBar.ITimeBarCallback() {
					public void onEndTimeValueChange(int hour, int minute) {
						sv.si.setEndTime(hour, minute);
						Log.i( "SHADOWS", "End: " + hour + ":" + minute  );
					}

					public void onStartTimeValueChange(int hour, int minute) {
						sv.si.setTime(hour, minute);
						Log.i( "SHADOWS", "Start: " + hour + ":" + minute  );
					}
				}, false);
        timeSeekBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				80, 0));
        
        ll.addView(timeSeekBar);
        
        sv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View view,
                    ContextMenuInfo menuInfo) {
		        MenuInflater inflater = getMenuInflater();
		        inflater.inflate(R.menu.test, menu);
			}});
        
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
            showDialog(DIALOG_DATEPICKER);
        	return true;
        case R.id.menu_config:
        	return true;        
        }
        return false;
    }    

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_now:
        case R.id.menu_goto:
            showDialog(DIALOG_DATEPICKER);
        	return true;
        case R.id.menu_config:
        	return true;        
        }
        return false;
    }    

	static final int DIALOG_DATEPICKER = 0;
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d;

        switch (id) {
        case DIALOG_DATEPICKER:
            d = new DatePickerDialog(
                    ShadowsActivity.this,
                    this,
                    2008,
                    1,
                    1);
            d.setTitle("Seleccione el día");
            break;
        default:
            d = null;
        }

        return d;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
        case DIALOG_DATEPICKER:
            DatePickerDialog datePicker = (DatePickerDialog)dialog;
            Calendar c = sv.si.getCalendar();
            datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            break;
        }
    }


	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		sv.si.setDate(year, month, day);
		Log.i( "DatePicker", ""+year+"-"+month+"-"+day );
	}
    
}