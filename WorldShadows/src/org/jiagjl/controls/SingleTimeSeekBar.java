package org.jiagjl.controls;

import org.jiagjl.R;
import org.jiagjl.controls.TimeSeekBar.ITimeBarCallback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.widget.SeekBar;

public class SingleTimeSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener  {

	private Paint paint;
	private Drawable startThumbNormal;
	
	private float labelSize = 12f;
	private float labelSizeHighlight = 24f;
	private float textSize = labelSize;
	
	
	public SingleTimeSeekBar(Context context, float minTime, float maxTime,
			int minuteGap, int preferredTimeWindow, ITimeBarCallback callback,
			boolean lightBackground) {
		super(context);
		setOnSeekBarChangeListener(this);
		startThumbNormal = this.getResources().getDrawable(R.drawable.seek_thumb_normal);
		setThumb(startThumbNormal);
		paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1.1F);
		paint.setColor( Color.BLACK );	
		setMax(100);
		
		
		this.callback = callback;
		
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.minuteGap = minuteGap;
		this.time_window = preferredTimeWindow / time_length;
		
		time_length = maxTime - minTime;
		max_range = 60.0f / minuteGap;
		
		setTime( minTime );
		
	}

	static float ATTRACTOR = 0.02f;
	static float ATTRACTOR_MIN = ATTRACTOR;
	static float ATTRACTOR_MAX = 1.0f-ATTRACTOR;
	
	String min_label = "";
	float minValue;
	
	float minTime, maxTime;
	int minuteGap;
	float time_window;
	
	float time_length;
	float max_range;

	protected ITimeBarCallback callback;
	
	public void setTime( float startTime ) {
        if ( startTime < minTime )
        	startTime = minTime;
        if ( startTime > maxTime )
        	startTime = maxTime-(minuteGap/60f);

        //Pasa los límites horarios mínimo y máximo al formato del SeekBar
        //y llama a los correspondientes métodos para cambiarlos
        setProgress((int)((startTime - minTime) / time_length * 100));
	}

	@Override
	synchronized protected final void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		paint.setTextSize(textSize);
		float half_text_width = paint.measureText(min_label) / 2f;
		float half_bounds_width = startThumbNormal.getIntrinsicWidth() / 4f;
		float left_padding = this.getPaddingLeft();
		float x = left_padding + startThumbNormal.getBounds().left + half_bounds_width - half_text_width;
		canvas.drawText(min_label, x, labelSizeHighlight, paint);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int newValue, boolean fromUser) {
		minValue = newValue/100.0f;
		if ( minValue < ATTRACTOR_MIN )
			minValue = 0f;
		if ( minValue > ATTRACTOR_MAX )
			minValue = 1.0f;

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

		invalidate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		textSize = labelSizeHighlight;
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		textSize = labelSize;
		invalidate();
	}

}
