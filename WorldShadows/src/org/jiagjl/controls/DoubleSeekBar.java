package org.jiagjl.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Abstract SeekBar (slider) class that supports the selection of an interval.
 * For this purpose, two thumbs are displayed and draggable. For concrete
 * implementations, see {@link HorizontalDoubleSeekBar} or
 * {@link VerticalDoubleSeekBar}.
 * 
 * @author joni
 * 
 */
public abstract class DoubleSeekBar extends View {

	// tag for logging
	public static final String LOG_TAG = "WorldShadows";

	// colors
	public static final int GREY = Color.parseColor("#9c9c9c");
	public static final int DARK_GREY = Color.parseColor("#5a5a5a");
	public static final int ORANGE = Color.parseColor("#ffd300");
	public static final int DARK_ORANGE = Color.parseColor("#ffb600");
	public static final int RED = Color.parseColor("#f00000");

	public static final long SLEEP_AFTER_TOUCH_EVENT = 50L;

	/**
	 * Tolerance for MOVE touch events in pixels. Only events above this
	 * tolerance will be taken into account.
	 */
	private final static float TOUCH_MOVE_TOLERANCE = 1f;

	/**
	 * Tolerance for DOWN touch events in pixels: Events with a larger distance
	 * to the bar will be ignored.
	 */
	private final static float TOUCH_DOWN_TOLERANCE = 15f;

	/**
	 * Minimum offset of the positions of the two thumbs, in pixels.
	 */
	protected final static int MINIMUM_THUMB_OFFSET = 15;

	protected int barThickness = 22;
	protected int barPadding = 4;

	private float startValue = 0f;
	private float endValue = 1f;

	protected float labelSize = 12f;
	protected float labelSizeHighlight = 24f;

	/**
	 * Minimum offset (relative value) of start and end value, calculated upon
	 * resizing from MINIMUM_THUMB_OFFSET
	 */
	private float minOffset = 0f;
	protected int startOffset;
	protected int endOffset;
	/**
	 * Size of the bar (not of the entire control, excluding start and end
	 * offset)
	 */
	protected int size;

	protected RectF backgroundRect;
	protected Drawable startThumb;
	protected Drawable startThumbNormal;
	protected Drawable startThumbActive;
	protected Drawable endThumb;
	protected Drawable endThumbNormal;
	protected Drawable endThumbActive;
	protected final Rect selectionRect = new Rect();
	protected int halfAThumb = -1;

	protected int startLabelX = 0;
	protected int startLabelY = 0;
	protected int endLabelX = 0;
	protected int endLabelY = 0;
	protected String startLabel;
	protected String endLabel;
	//
	// private float touchX = -5f;
	// private float touchY = -5f;

	protected final static int NONE = 0;
	protected final static int START = 1;
	protected final static int END = 2;

	protected int thumbDown = NONE;

	protected final Paint paint = new Paint();
	protected final Paint highlightPaint = new Paint();
	protected LinearGradient backgroundGradient;
	protected LinearGradient selectionGradient;

	protected IDoubleSeekBarCallback callback;

	private boolean _lightBackground = false;

	protected float[] _photoMarks = new float[]{};

	/**
	 * Creates a new {@link DoubleSeekBar}.
	 * 
	 * @param context
	 *            The application's context
	 * @param callback
	 *            The callback used for interaction with the model
	 * @param lightBackground
	 *            Whether the DoubleSeekBar is drawn on a light (
	 *            <code>true</code>) or a dark (<code>false</code>) background
	 */
	public DoubleSeekBar(final Context context,
			IDoubleSeekBarCallback callback, boolean lightBackground) {
		super(context);
		_lightBackground = lightBackground;

		setCallback( callback );

		this.paint.setStyle(Style.FILL);
		this.paint.setAntiAlias(true);
		this.paint.setTextSize(this.labelSize);
		this.paint.setStrokeWidth(1.1F);

		// Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");
	}

	protected final void initialize() {
		this.startOffset = this.halfAThumb;
		this.endOffset = this.halfAThumb;
	}

	@Override
	protected final void onDraw(final Canvas canvas) {
		// this.updateAllBounds();

		super.onDraw(canvas);
		// paint.setColor(Color.GRAY);
		paint.setShader(backgroundGradient);
		canvas.drawRoundRect(this.backgroundRect, 5f, 5f, paint);
		// paint.setColor(PhotoCompassApplication.ORANGE);
		paint.setShader(selectionGradient);
		canvas.drawRect(this.selectionRect, paint);

		// draw photo marks
		paint.setShader(null);
		paint.setColor(DoubleSeekBar.RED);

		this.drawPhotoMarks(canvas);

		startThumb.draw(canvas);
		endThumb.draw(canvas);

		paint.setShader(null);
		paint.setColor(_lightBackground ? Color.DKGRAY : Color.WHITE);
		// paint.setTextSize(10);
		// Log.d(Constants.LOG_TAG, "DoubleSeekBar: text size "
		// + paint.getTextSize());
		this.drawLabels(canvas);
		// paint.setColor(Color.RED);
		// canvas.drawCircle(this.touchX, this.touchY, 4, this.paint);
	}

	protected abstract void drawPhotoMarks(Canvas canvas);

	protected abstract void drawLabels(Canvas canvas);

	/**
	 * Updates size-dependent positions and values upon resizing. backgroundRect
	 * and size have to be updated beforehand by the subclass.
	 */
	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		Log.d(DoubleSeekBar.LOG_TAG, "DoubleSeekBar.onSizeChanged(), new size "
				+ this.size);
		this.minOffset = (float) MINIMUM_THUMB_OFFSET / this.size;
		this.updateStartBounds();
		this.updateEndBounds();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected final void callbackStartValue(float newValue) {
		// Log.d(Constants.LOG_TAG,
		// "DoubleSeekBar.callbackStartValue(" + newValue + ")");
		this.callback.onMinValueChange(this.tryStartValue(newValue));
	}

	protected final void callbackEndValue(final float newValue) {
		// Log.d(Constants.LOG_TAG,
		// "DoubleSeekBar.callbackEndValue(" + newValue + ")");
		this.callback.onMaxValueChange(this.tryEndValue(newValue));
	}

	public final void updateStartValue(float newValue) {
		// Log.d(Constants.LOG_TAG,
		// "DoubleSeekBar.updateStartValue() to " + newValue);
		this.setStartValue(newValue);
		this.startLabel = this.callback.getMinLabel();
		this.updateStartBounds();
	}

	public final void updateEndValue(final float newValue) {
		// Log.d(Constants.LOG_TAG,
		// "DoubleSeekBar.updateEndValue() to " + newValue);
		this.setEndValue(newValue);
		this.endLabel = this.callback.getMaxLabel();
		this.updateEndBounds();
	}

	protected abstract void updateStartBounds();

	protected abstract void updateEndBounds();

	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		// TODO check GestureDetector
		final int action = event.getAction();
		// Log.d(Constants.LOG_TAG,
		// "DoubleSeekBar: onTouchEvent: action = "+action);
		final float touchX = event.getX();
		final float touchY = event.getY();
		final float newValue = convertToAbstract(getEventCoordinate(event));
		if (action == MotionEvent.ACTION_DOWN) {
			// ignore if distance to bar larger than tolerance constant
			if ((this.backgroundRect.left - touchX) > TOUCH_DOWN_TOLERANCE
					|| (touchX - this.backgroundRect.right) > TOUCH_DOWN_TOLERANCE
					|| (this.backgroundRect.top - touchY) > TOUCH_DOWN_TOLERANCE
					|| (touchY - this.backgroundRect.bottom) > TOUCH_DOWN_TOLERANCE) {
				this.thumbDown = NONE;
				return false;
			}
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to start is less than distance to end
				this.thumbDown = START;
				this.startThumb = this.startThumbActive;
				this.callbackStartValue(newValue);
			} else {
				// distance to end is less than to start
				this.thumbDown = END;
				this.endThumb = this.endThumbActive;
				this.callbackEndValue(newValue);
			}
			this.invalidate(); // TODO determine "dirty" region
		} else if (action == MotionEvent.ACTION_MOVE && this.thumbDown != NONE) {
			if (this.thumbDown == START
					&& ((Math.abs(this.startValue - newValue) * this.size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE)) {
				this.callbackStartValue(newValue);
				this.invalidate();
			} else if (this.thumbDown == END
					&& (Math.abs(this.endValue - newValue) * this.size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE) {
				this.callbackEndValue(newValue);
				this.invalidate();
			}
		} else {
			if (action == MotionEvent.ACTION_UP && this.thumbDown != NONE) {
				if (this.thumbDown == START) {
					this.startThumb = this.startThumbNormal;
					this.callbackStartValue(newValue);
				} else {
					this.endThumb = this.endThumbNormal;
					this.callbackEndValue(newValue);
				}
				this.thumbDown = NONE;
				this.invalidate();
			} else {
				Log.w(DoubleSeekBar.LOG_TAG,
						"DoubleSeekBar: Unexpected TouchEvent, action "
								+ action);
			}

			// sleep to avoid event flooding
			try {
				// Log.d(Constants.LOG_TAG,
				// "DoubleSeekBar: sleep");
				Thread.sleep(DoubleSeekBar.SLEEP_AFTER_TOUCH_EVENT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * @return the start value (left slider thumb), as a float from the range
	 *         [0,1].
	 */
	public final float getStartValue() {
		return this.startValue;
	}

	/**
	 * @return the end value (right slider thumb), as a float from the range
	 *         [0,1].
	 */
	public final float getEndValue() {
		return this.endValue;
	}

	protected final float setStartValue(float newValue) {
		return this.startValue = this.tryStartValue(newValue);
	}

	protected final float setEndValue(float newValue) {
		return this.endValue = this.tryEndValue(newValue);
	}

	private final float tryStartValue(float newValue) {
		return Math.max(0f, Math.min(newValue, this.endValue - this.minOffset));
	}

	private final float tryEndValue(float newValue) {
		return Math.min(1f, Math
				.max(newValue, this.startValue + this.minOffset));
	}

	protected abstract float getEventCoordinate(final MotionEvent event);

	protected abstract int convertToConcrete(final float abstractValue);

	protected abstract float convertToAbstract(final float concreteValue);

	public final void setCallback(IDoubleSeekBarCallback callback) {
		this.callback = callback;

		if ( callback != null ) {
			this.setStartValue(callback.getMinValue());
			this.startLabel = callback.getMinLabel();
	
			this.setEndValue(callback.getMaxValue());
			this.endLabel = callback.getMaxLabel();
		}
	}

	public final void setPhotoMarks(final float[] photoMarks) {
		_photoMarks = photoMarks;
	}
}
