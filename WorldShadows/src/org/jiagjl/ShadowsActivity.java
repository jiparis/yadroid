package org.jiagjl;

import java.util.Calendar;

import org.jiagjl.controls.TimeSeekBar;
import org.jiagjl.controls.SingleTimeSeekBar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShadowsActivity extends Activity implements DatePickerDialog.OnDateSetListener {
	
    ShadowsView sv;
    View timeSeekBar;
    boolean mSingle = true;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
//        showDialog(PROGRESS_DIALOG);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        												LayoutParams.WRAP_CONTENT));
        TextView tv = new TextView(getApplicationContext()); 
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT, 1));
        ll.addView(tv);        

        sv = new ShadowsView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        												LayoutParams.FILL_PARENT, 1));
        sv.setClickable(true);
        
        float min_time = 8f;
        float max_time = 22f;
        Calendar c = sv.solarInformation.getCalendar();
        float start_time = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0f;
        
        if ( mSingle ) {
	        timeSeekBar = new SingleTimeSeekBar(getApplicationContext(),
					min_time, max_time, 5, new TimeSeekBar.ITimeBarCallback() {
						public void onEndTimeValueChange(int hour, int minute) {
						}
						public void onStartTimeValueChange(int hour, int minute) {
							sv.solarInformation.setTime(hour, minute);
							//Log.i( "SHADOWS", "Start: " + hour + ":" + minute  );
						}
					});
	        timeSeekBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 75, 0));
	        SingleTimeSeekBar tsb = (SingleTimeSeekBar)timeSeekBar;
	        tsb.setProgressDrawable(getResources().getDrawable(R.xml.seekbar));
	        tsb.setPadding(2, 25, 2, 20);
	        setTimeLimits();
        } else {
	        timeSeekBar = new TimeSeekBar(getApplicationContext(), min_time,
	        		max_time, 30, (int)sv.solarInformation.getValue(SolarInformation.TIME_WINDOW_VALUE) / 60,
	        		new TimeSeekBar.ITimeBarCallback() {
						public void onEndTimeValueChange(int hour, int minute) {
							sv.solarInformation.setEndTime(hour, minute);
							//Log.i( "SHADOWS", "End: " + hour + ":" + minute  );
						}
						public void onStartTimeValueChange(int hour, int minute) {
							sv.solarInformation.setTime(hour, minute);
							//Log.i( "SHADOWS", "Start: " + hour + ":" + minute  );
						}
					}, false);
	        timeSeekBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 80, 0));
        }

        sv.solarInformation.setTimeWindow(SolarInformation.DEFAULT_TIME_WINDOW);
        setTime( start_time );
        
        ll.addView(timeSeekBar);
        
//        sv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//			public void onCreateContextMenu(ContextMenu menu, View view,
//                    ContextMenuInfo menuInfo) {
//		        MenuInflater inflater = getMenuInflater();
//		        inflater.inflate(R.menu.test, menu);
//			}});
        
        setContentView(sv);

        addContentView(ll, new LayoutParams
        		(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }

	public void setTime( float startTime ) {
        if ( !mSingle )
        	((TimeSeekBar)timeSeekBar).setTime( startTime );
        else
        	((SingleTimeSeekBar)timeSeekBar).setTime( startTime );
	}
    
	public void setTimeLimits() {
        float sunrise = (float) sv.solarInformation.getValue(SolarInformation.SUNRISE_VALUE);
        float sunset = (float) sv.solarInformation.getValue(SolarInformation.SUNSET_VALUE); 
        float min_time = (float)Math.floor( sunrise - 1.5f );
        float max_time = (float)Math.ceil( sunset + 1.5f ); 
        if ( !mSingle )
        	;
        else {
	        SingleTimeSeekBar tsb = (SingleTimeSeekBar)timeSeekBar;
	        float time = tsb.getTime();
	        tsb.setTimeLimits(min_time, max_time);
	        tsb.setTimeMarks(new float[] { sunrise, sunset }, new String[] { getText(R.string.txt_sunrise) + timeToString(sunrise), getText(R.string.txt_sunset) + timeToString(sunset) });
	        tsb.setTime(time);
        }
//        Log.i("", "" + min_time + " - " + sunrise + " - " + max_time + " - " + sunset);
	}
    
	//Cálculo de la hora y los minutos a partir de la hora militar
	static public String timeToString( float time ) {
		String minute, hour;
		int t = (int) Math.floor(time);
		int m = (int) Math.floor((time - t) * 60f );
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		
		return hour + ":" + minute;
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
        	sv.solarInformation.now();
            Calendar c = sv.solarInformation.getCalendar();
            float start_time = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0f;
        	//Si no se invalida primero, no se pinta bien el nuevo valor del slider
        	timeSeekBar.invalidate();
        	setTime(start_time);
        	setTimeLimits();
    		sv.must_init_labels = true;
        	return true;
        case R.id.menu_goto:
            showDialog(DIALOG_DATEPICKER);
        	return true;
        case R.id.menu_config:
        	sv.toggleDec = (sv.toggleDec+1)%3; 
        	return true;        
        case R.id.menu_help:
            showDialog(HELP_DIALOG);
        	return true;
        }
        return false;
    }    

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.menu_now:
//        	sv.solarInformation.now(); 
//            Calendar c = sv.solarInformation.getCalendar();
//            float start_time = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0f;
//        	//Si no se invalida primero, no se pinta bien el nuevo valor del slider
//        	timeSeekBar.invalidate();
//        	setTime(start_time);
//        	setTimeLimits();
//    		sv.must_init_labels = true;
//        	return true;
//        case R.id.menu_goto:
//            showDialog(DIALOG_DATEPICKER);
//        	return true;
////        case R.id.menu_config:
////        	sv.toggleX = !sv.toggleX; 
////        	return true;        
//        }
//        return false;
//    }    

	static final int DIALOG_DATEPICKER = 0;
    static final int PROGRESS_DIALOG = 1;
    static final int HELP_DIALOG = 2;
   
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = null;

        switch (id) {
        case DIALOG_DATEPICKER:
            d = new DatePickerDialog(ShadowsActivity.this, this, 2008, 1, 1);
            d.setTitle(getText(R.string.txt_day_selection));
            break;
        case PROGRESS_DIALOG:
//            d = new ProgressDialog(ShadowsActivity.this);
////            ProgressDialog.show(getApplicationContext(), "", "Loading. Please wait...", true);
//            ((ProgressDialog)d).setMessage("Loading. Please wait...");
            break;
        case HELP_DIALOG:
          d = new Dialog(ShadowsActivity.this);
          d.setContentView(R.layout.help);
          d.setTitle(R.string.app_title);
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
            Calendar c = sv.solarInformation.getCalendar();
            datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            break;
        }
    }


	public void onDateSet(DatePicker view, int year, int month, int day) {
		sv.solarInformation.setDate(year, month, day);
		sv.must_init_labels = true;
        setTimeLimits();
//		Log.i( "DatePicker", ""+year+"-"+month+"-"+day );
	}
    
}