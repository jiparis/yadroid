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
	
	static final int MAX_PROGRESS = 200;
	
	public SingleTimeSeekBar(Context context, float minTime, float maxTime,
			int minuteGap, ITimeBarCallback callback ) {
		super(context);
		setOnSeekBarChangeListener(this);
		startThumbNormal = this.getResources().getDrawable(R.drawable.seek_thumb_normal);
		setThumb(startThumbNormal);
		paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1.1F);
		paint.setColor( Color.BLACK );	
		setMax(MAX_PROGRESS);
		
		this.callback = callback;

		this.minuteGap = minuteGap;
		this.max_range = 60.0f / minuteGap;
		
		setTimeLimits( minTime, maxTime );
	}

	public void setTimeLimits( float minTime, float maxTime ) {
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.time_length = maxTime - minTime;
		
//		setTime( minTime );
	}
	
	static final int ATTRACTOR = 0;
	static final int ATTRACTOR_MIN = ATTRACTOR;
	static final int ATTRACTOR_MAX = MAX_PROGRESS-ATTRACTOR;
	
	String min_label = "";
	float minValue;
	
	float minTime, maxTime;
	int minuteGap;
	
	float time_length;
	float max_range;

	protected ITimeBarCallback callback;
	
	public void setTime( float startTime ) {
        if ( startTime < minTime )
        	startTime = minTime;
        if ( startTime > maxTime )
        	startTime = maxTime-(minuteGap/60f);

//		Log.i("setTime", "" + startTime );
        setProgress( timeToProgress( startTime ) );
	}

	private float[] mMarks;
	private String[] mLabels;
	private boolean callback_paused = false;

	public void setCallbackPaused( boolean paused ) {
		callback_paused = paused;
	}
	
	public void setTimeMarks( float[] marks, String[] labels ) {
		mMarks = marks;
		mLabels = labels;
        if (!firstTime ) {
            int progress = getProgress();
            setCallbackPaused( true );
            for ( int n = 0; n < mMarks.length; n++ ) {
		        setProgress( timeToProgress(mMarks[n]));
		        mMarks[n] = startThumbNormal.getBounds().left + startThumbNormal.getIntrinsicWidth()/2;
//		        Log.i("mMarks[n]", ""+mMarks[n]);
			}
	        setProgress( progress );
            setCallbackPaused( false );
        }
	}
	
	public int timeToProgress( float time ) {
		return (int)((time - minTime) / time_length * MAX_PROGRESS);
	}
	
	public float progressToTime( int progress ) {
		return (progress/(float)MAX_PROGRESS*time_length+minTime);
	}

	public float getTime() {
		return progressToTime(getProgress());
	}
	

	int w;
	int h;
    int pt;
    int pb;
    int pl;
    int pr;
    int trackHeight;
    int max;
    int available;
    int gapForCenteringTrack;

    void initDraw() {
		w = getWidth();
		h = getHeight();
        pt = getPaddingTop();
        pb = getPaddingBottom();
        pl = getPaddingLeft();
        pr = getPaddingRight();
        trackHeight = h - pt - pb;
        max = getMax();
        available = getWidth() - pl - pr + getThumbOffset() * 2;
        gapForCenteringTrack = (startThumbNormal.getIntrinsicHeight() - trackHeight) / 2;
    }
	
    boolean firstTime = true;
    
	@Override
	synchronized protected final void onDraw(final Canvas canvas) {
		if ( firstTime ) {
			firstTime = false;
			setTimeMarks( mMarks, mLabels );
		}
			
		super.onDraw(canvas);
		initDraw();
		canvas.save();
		float half_bounds_width = startThumbNormal.getIntrinsicWidth() / 4f;
		float left_padding = this.getPaddingLeft();
		float x = left_padding + startThumbNormal.getBounds().left + half_bounds_width;
		drawText( canvas, min_label, (int)x, 0, textSize );
		drawMarks( canvas);
		canvas.restore();

		if (startThumbNormal != null) {
            canvas.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
            startThumbNormal.draw(canvas);
            canvas.restore();
        }
	}

	synchronized protected void drawText( Canvas canvas, String text, int x, int vAlign, float textSize ) {
		paint.setTextSize(textSize);
		paint.setColor(Color.BLACK);
		float text_width = paint.measureText(text);
		x = x - (int)(text_width / 2f);
		if ( x < 4 )
			x = 4;
		else if ( x + text_width > w )
			x = w-(int)text_width;
		
		canvas.drawText(text, x, (vAlign == 0 ? pt-4 /*labelSizeHighlight*/ : h - pb + paint.getFontSpacing()), paint );
	}
	
	public static final int DARK_ORANGE = Color.parseColor("#ffb600");

	synchronized protected final void drawMarks(final Canvas canvas) {
		if ( mMarks == null || mMarks.length == 0 )
			return;

        int y1 = gapForCenteringTrack + pt+4; //El 4 es elnúmero mágico, porque no se como calcular bien la posición
        int y2 = h - pb - gapForCenteringTrack-4;

        for ( int n = 0; n < mMarks.length; n++ ) {
            float scale = max > 0 ? (float) timeToProgress(mMarks[n]) / (float) max : 0;
	        int markPos = (int) (scale * available);
	        markPos = (int)mMarks[n];
			paint.setColor( DARK_ORANGE );
	        canvas.drawLine(markPos, y1, markPos, y2, paint);
	        drawText(canvas, mLabels[n], markPos, 1, labelSize);
        }
	}
	
	public void onProgressChanged(SeekBar seekBar, int newValue, boolean fromUser) {
		if ( callback_paused )
			return;
		
		minValue = newValue; 
		if ( minValue < ATTRACTOR_MIN )
			minValue = 0f;
		if ( minValue > ATTRACTOR_MAX )
			minValue = MAX_PROGRESS;

		//Cálculo de la hora militar a partir de los valores del DoubleSeekBar
		String minute, hour;
		float time = progressToTime((int)minValue);
		//Cálculo de la hora y los minutos a partir de la hora militar
		int t = (int) Math.floor(time);
		int m = (int) Math.floor((time - t) * max_range)*minuteGap;
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		min_label = hour + ":" + minute;

//		Log.i("TIME", t + ":" + m );
		//Llamada al callback
		callback.onStartTimeValueChange(t, m);

		invalidate();
	}

	
	public void onStartTrackingTouch(SeekBar arg0) {
		textSize = labelSizeHighlight;
	}

	
	public void onStopTrackingTouch(SeekBar arg0) {
		textSize = labelSize;
		invalidate();
	}

}
