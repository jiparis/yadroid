package org.jiagjl.controls;

import android.content.Context;

public class TimeSeekBar extends HorizontalDoubleSeekBar implements IDoubleSeekBarCallback{

	static public interface ITimeBarCallback {
		public void onStartTimeValueChange( int hour, int minute );
		public void onEndTimeValueChange( int hour, int minute );
	}

	static float ATTRACTOR = 0.02f;
	static float ATTRACTOR_MIN = ATTRACTOR;
	static float ATTRACTOR_MAX = 1.0f-ATTRACTOR;
	
	float minTime, maxTime;
	int minuteGap;
	float time_window;
	
	float time_length;
	float max_range;

	float minValue = 0;
	float maxValue = 0;

	String max_label = "";
	String min_label = "";

	protected ITimeBarCallback callback;
	
    // Todas las horas son en formato militar, es decir, un float donde la parte
	// entera es la hora, y la parte decimal son los minutos pero como un
	// continuo entre cero y uno.
	// De esta forma las 10:30 sería el número 10.5f
	
	// minTime: Hora límite inferior
	// maxTime: Hora límite superior
	// minuteGap: Granularidad en el paso de los minutos. Un valor de10 hará que
	//            los minutos pasen de 10 en 10
	// preferredTimeWindow: Distancia de tiempo preferida entre start y end 
	// endTime:   Hora de fin inicial
	// callback: Clase que va a ser invocada cada vez que cambie la hora de
	//           comienzo o la de fin
	// lightBackground: Indica si atenúa las horas mostradas
	public TimeSeekBar(Context context, float minTime, float maxTime,
			int minuteGap, int preferredTimeWindow, ITimeBarCallback callback,
			boolean lightBackground) {
		
		// Le pongo un callback nulo, para luego poder pasarle el callback
		// bueno, una vez que ya existe un "this"
		super(context, null, lightBackground);

		this.callback = callback;
		
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.minuteGap = minuteGap;
		this.time_window = preferredTimeWindow / time_length;
		
		time_length = maxTime - minTime;
		max_range = 60.0f / minuteGap;
		
		// Callback bueno. Aquí es donde se hace toda la lógica de conversión
		// entre horas y valores del DoubleSeekBar, y además se llama al callbak
		// del tiempo cada vez que cambia el rango dehoras seleccionadas
		setCallback( this );

		//Se hace el set time una vez que está puesto el callback correcto
		setTime( minTime );
	}

	public void setTime( float startTime, float endTime) {
        if ( startTime < minTime )
        	startTime = minTime;
        if ( endTime > maxTime )
        	endTime = maxTime;

        //Pasa los límites horarios mínimo y máximo al formato del DoubleSeekBar
        //y llama a los correspondientes métodos para cambiarlos
		onMinValueChange((startTime - minTime) / time_length);
		onMaxValueChange((endTime - minTime) / time_length);
	}
	
	public void setTime( float startTime ) {
        if ( startTime < minTime )
        	startTime = minTime;
        if ( startTime > maxTime )
        	startTime = maxTime-(minuteGap/60f);

        //Pasa los límites horarios mínimo y máximo al formato del DoubleSeekBar
        //y llama a los correspondientes métodos para cambiarlos
		onMinValueChange((startTime - minTime) / time_length);
	}
	
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
		//Redondeo y ajuste de límites
		maxValue = Math.round(newValue*100)/100.0f;
		if ( maxValue > ATTRACTOR_MAX )
			maxValue = 1.0f;
		
		//Cálculo de la hora militar a partir de los valores del DoubleSeekBar
		String minute, hour;
		float time = maxValue*time_length+minTime;
		//Cálculo de la hora y los minutos a partir de la hora militar
		int t = (int) Math.floor(time);
		int m = (int) Math.floor((time - t) * max_range)*minuteGap;
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		max_label = hour + ":" + minute;

		//Llamada al callback
		callback.onEndTimeValueChange(t, m);
		
		//Repintar
		super.updateEndValue(maxValue);
		time_window = maxValue-minValue;
	}

	public void onMinValueChange(float newValue) {
		//Redondeo y ajuste de límites
		minValue = Math.round(newValue*100)/100.0f;
		if ( minValue < ATTRACTOR_MIN )
			minValue = 0f;

		//Cálculo de la hora militar a partir de los valores del DoubleSeekBar
		String minute, hour;
		float time = minValue*time_length+minTime;
		//Cálculo de la hora y los minutos a partir de la hora militar
		int t = (int) Math.floor(time);
		int m = (int) Math.floor((time - t) * max_range)*minuteGap;
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		min_label = hour + ":" + minute;

		//Llamada al callback
		callback.onStartTimeValueChange(t, m);

		//Repintar
		this.updateStartValue(minValue);

		onMaxValueChange( minValue+time_window );
	}
	
	//Cálculo de la hora y los minutos a partir de la hora militar
//	static private String toHoursMinutes( float time, float maxRange, int minuteGap ) {
//		String minute, hour;
//		int t = (int) Math.floor(time);
//		int m = (int) Math.floor((time - t) * maxRange)*minuteGap;
//		if (m < 10) minute = "0" + m; else minute = ""+m;
//		if (t < 10) hour = "0" + t; else hour = ""+t;
//		
//		return hour + ":" + minute;
//	}
}
