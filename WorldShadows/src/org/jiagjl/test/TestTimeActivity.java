package org.jiagjl.test;

import org.jiagjl.R;
import org.jiagjl.controls.TimeSeekBar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestTimeActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);

		// Crear un barra para selección de un rango de horas situado entre las
		// 8:00 y las 22:30, con los sliders posicionados en el valor inicial de
		// 10:30 a 18:00, y con una granularidad de movimiento de 30 minutos.
		// Los valores horarios los vuelca en el log
		TimeSeekBar timeSeekBar = new TimeSeekBar(getApplicationContext(), 8f,
				22f, 30, 10.5f, 18f, new TimeSeekBar.ITimeBarCallback() {
					public void onEndTimeValueChange(int hour, int minute) {
						Log.i( "SHADOWS", "End: " + hour + ":" + minute  );
					}

					public void onStartTimeValueChange(int hour, int minute) {
						Log.i( "SHADOWS", "Start: " + hour + ":" + minute  );
					}
				}, false);

		setContentView(timeSeekBar);
	}
}
