package org.jiagjl.controls;

import android.content.Context;

public class TimeSeekBar extends HorizontalDoubleSeekBar {

	static public interface ITimeBarCallback {
		public void onStartTimeValueChange( int hour, int minute );
		public void onEndTimeValueChange( int hour, int minute );
	}

	static float ATTRACTOR = 0.02f;
	static float ATTRACTOR_MIN = ATTRACTOR;
	static float ATTRACTOR_MAX = 1.0f-ATTRACTOR;
	
	protected ITimeBarCallback callback;
	
    // Todas las horas son en formato militar, es decir, un float donde la parte
	// entera es la hora, y la parte decimal son los minutos pero como un
	// continuo entre cero y uno.
	// De esta forma las 10:30 ser�a el n�mero 10.5f
	
	// minTime: Hora l�mite inferior
	// maxTime: Hora l�mite superior
	// minuteGap: Granularidad en el paso de los minutos. Un valor de10 har� que
	//            los minutos pasen de 10 en 10
	// startTime: Hora de comienzo inicial
	// endTime:   Hora de fin inicial
	// callback: Clase que va a ser invocada cada vez que cambie la hora de
	//           comienzo o la de fin
	// lightBackground: Indica si aten�a las horas mostradas
	public TimeSeekBar(final Context context, final float minTime,
			final float maxTime, final int minuteGap, final float startTime,
			final float endTime, final ITimeBarCallback callback,
			boolean lightBackground) {
		
		// Le pongo un callback nulo, para luego poder pasarle el callback
		// bueno, una vez que ya existe un "this"
		super(context, null, lightBackground);

		final float time_length = maxTime - minTime;
		final float max_range = 60.0f / minuteGap;

		// Callback bueno. Aqu� es donde se hace toda la l�gica de conversi�n
		// entre horas y valores del DoubleSeekBar, y adem�s se llama al callbak
		// del tiempo cada vez que cambia el rango dehoras seleccionadas
		setCallback( new IDoubleSeekBarCallback() {
			//Pasa los l�mites horarios m�nimo y m�ximo al formato del DoubleSeekBar
			float minValue = (startTime - minTime) / time_length;
			float maxValue = (endTime - minTime) / time_length;
			//Calcula la representaci�n gr�fica de los l�mites horarios m�nimo y m�ximo
			String max_label = toHoursMinutes(maxValue*time_length+minTime, max_range, minuteGap);
			String min_label = toHoursMinutes(minValue*time_length+minTime, max_range, minuteGap);

			public String getMaxLabel() {
				return max_label;
			}

			public float getMaxValue() {
				return maxValue;
			}

			public String getMinLabel() {
				return min_label;
			}

			public float getMinValue() {
				return minValue;
			}

			public void onMaxValueChange(float newValue) {
				//Redondeo y ajuste de l�mites
				maxValue = Math.round(newValue*100)/100.0f;
				if ( maxValue > ATTRACTOR_MAX )
					maxValue = 1.0f;
				
				//C�lculo de la hora militar a partir de los valores del DoubleSeekBar
				String minute, hour;
				float time = maxValue*time_length+minTime;
				//C�lculo de la hora y los minutos a partir de la hora militar
				int t = (int) Math.floor(time);
				int m = (int) Math.floor((time - t) * max_range)*minuteGap;
				if (m < 10) minute = "0" + m; else minute = ""+m;
				if (t < 10) hour = "0" + t; else hour = ""+t;
				max_label = hour + ":" + minute;

				//Llamada al callback
				callback.onEndTimeValueChange(t, m);
				
				//Repintar
				TimeSeekBar.super.updateEndValue(maxValue);
			}

			public void onMinValueChange(float newValue) {
				//Redondeo y ajuste de l�mites
				minValue = Math.round(newValue*100)/100.0f;
				if ( minValue < ATTRACTOR_MIN )
					minValue = 0f;

				//C�lculo de la hora militar a partir de los valores del DoubleSeekBar
				String minute, hour;
				float time = minValue*time_length+minTime;
				//C�lculo de la hora y los minutos a partir de la hora militar
				int t = (int) Math.floor(time);
				int m = (int) Math.floor((time - t) * max_range)*minuteGap;
				if (m < 10) minute = "0" + m; else minute = ""+m;
				if (t < 10) hour = "0" + t; else hour = ""+t;
				min_label = hour + ":" + minute;

				//Llamada al callback
				callback.onStartTimeValueChange(t, m);

				//Repintar
				TimeSeekBar.this.updateStartValue(minValue);
			}
		} );
	}

	//C�lculo de la hora y los minutos a partir de la hora militar
	static private String toHoursMinutes( float time, float maxRange, int minuteGap ) {
		String minute, hour;
		int t = (int) Math.floor(time);
		int m = (int) Math.floor((time - t) * maxRange)*minuteGap;
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		
		return hour + ":" + minute;
	}
}
