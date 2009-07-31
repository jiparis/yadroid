package org.jiagjl;

import android.app.Activity;
import android.os.Bundle;

public class ShadowsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(new ShadowsView(this));
    }
}