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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(new ShadowsView(this));
    }
    

    /* Creates the menu items */
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
        }
        return false;
    }    
}