package org.andamobile.ashadow.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

import org.andamobile.ashadow.R;

/**
 * SeekBar (slider) control with two thumbs for horizontal display. Displays
 * labels above the thumbs. Label values are retrieved from the Callback.
 */
public class HorizontalDoubleSeekBar extends DoubleSeekBar {
	private final int topPadding = 29;
	private static final int LABEL_PADDING = 5;

	/**
	 * Creates a new HorizontalDoubleSeekBar using the application's Context and
	 * a custom callback that is used to retrieve the labels as well as to
	 * notify the application model about changes.
	 * 
	 * @param context
	 * @param callback
	 */
	public HorizontalDoubleSeekBar(final Context context,
			final IDoubleSeekBarCallback callback, boolean lightBackground) {
		super(context, callback, lightBackground);
		Resources res = this.getResources();
		this.startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.startThumb = this.startThumbNormal;
		this.endThumb = this.endThumbNormal;
		this.halfAThumb = this.startThumb.getIntrinsicWidth() / 2;
		this.initialize();
		this.selectionRect.top = this.barPadding + this.topPadding;
		this.selectionRect.bottom = this.barThickness + this.barPadding
				+ this.topPadding;
		this.paint.setTextAlign(Align.CENTER);
		backgroundGradient = new LinearGradient(0, topPadding + barPadding, 0,
				topPadding + barPadding + barThickness / 2, DoubleSeekBar.GREY,
				DoubleSeekBar.DARK_GREY, Shader.TileMode.MIRROR);
		selectionGradient = new LinearGradient(0, topPadding + barPadding, 0,
				topPadding + barPadding + barThickness / 2,
				DoubleSeekBar.ORANGE, DoubleSeekBar.DARK_ORANGE,
				Shader.TileMode.MIRROR);

		this.startLabelY = topPadding - 4;
		this.endLabelY = topPadding - 4;
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		this.size = w - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(0f, topPadding + barPadding, w,
				barThickness + barPadding + topPadding);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected void updateStartBounds() {
		int begin = convertToConcrete(this.getStartValue()) - halfAThumb;
		this.startThumb.setBounds(begin, topPadding, begin
				+ this.startThumb.getIntrinsicWidth(), this.startThumb
				.getIntrinsicHeight()
				+ topPadding);
		this.selectionRect.left = begin + halfAThumb;
		this.startLabelX = this.startThumb.getBounds().centerX();
	}

	protected void updateEndBounds() {
		int begin = convertToConcrete(this.getEndValue()) - halfAThumb;
		this.endThumb.setBounds(begin, topPadding, begin
				+ this.startThumb.getIntrinsicWidth(), this.startThumb
				.getIntrinsicHeight()
				+ topPadding);
		this.selectionRect.right = begin + halfAThumb;
		this.endLabelX = this.endThumb.getBounds().centerX();
	}

	@Override
	protected int convertToConcrete(final float abstractValue) {

		return Math.round(abstractValue * this.size) + this.startOffset;

	}

	@Override
	protected float convertToAbstract(final float concreteValue) {
		return (float) (concreteValue - this.startOffset) / this.size;

	}

	@Override
	protected float getEventCoordinate(final MotionEvent event) {
		return event.getX();
	}

	@Override
	protected void drawPhotoMarks(Canvas canvas) {
		float pos;
		for (float mark : _photoMarks) {
			pos = backgroundRect.left + startOffset + mark * size;
			canvas.drawLine(pos, backgroundRect.top, pos,
					backgroundRect.bottom, paint);
		}
	}

	@Override
	protected void drawLabels(Canvas canvas) {
		// float slWidth2 = this.paint.measureText(this.startLabel) / 2;
		// float elWidth2 = this.paint.measureText(this.endLabel) / 2;
		// float slX = this.startLabelX;
		// float elX = this.endLabelX;
		float slWidth2;
		float elWidth2;
		float slX = this.startLabelX;
		float elX = this.endLabelX;

		if (this.thumbDown == START) {
			this.paint.setTextSize(this.labelSizeHighlight);
			slWidth2 = this.paint.measureText(this.startLabel) / 2;

			this.paint.setTextSize(this.labelSize);
			elWidth2 = this.paint.measureText(this.endLabel) / 2;
		} else {
			slWidth2 = this.paint.measureText(this.startLabel) / 2;

			if (this.thumbDown == END) {
				this.paint.setTextSize(this.labelSizeHighlight);
				elWidth2 = this.paint.measureText(this.endLabel) / 2;
				this.paint.setTextSize(this.labelSize);
			} else {
				elWidth2 = this.paint.measureText(this.endLabel) / 2;
			}
		}

		if (2 * slWidth2 + 2 * elWidth2 + LABEL_PADDING <= this.getWidth()) {
			if ((elX - elWidth2) - (slX + slWidth2) < LABEL_PADDING) {
				float offset = ((slWidth2 + elWidth2 + LABEL_PADDING) - (elX - slX)) / 2;
				float startOffset = offset;
				float endOffset = offset;
				if (offset > slWidth2 - (float) MINIMUM_THUMB_OFFSET / 2
						+ (float) LABEL_PADDING / 2) {
					startOffset = slWidth2 - (float) MINIMUM_THUMB_OFFSET / 2
							+ (float) LABEL_PADDING / 2;
					endOffset = offset + (offset - startOffset);
				} else if (offset > elWidth2 - (float) MINIMUM_THUMB_OFFSET / 2
						+ (float) LABEL_PADDING / 2) {
					endOffset = elWidth2 - (float) MINIMUM_THUMB_OFFSET / 2
							+ (float) LABEL_PADDING / 2;
					startOffset = offset + (offset - endOffset);
				}
				slX -= startOffset;
				elX += endOffset;
			}
			if (slX - slWidth2 < 0) {
				slX = slWidth2;
				elX = Math.max(elX, slX + slWidth2 + elWidth2 + LABEL_PADDING);
			}
			if (elX + elWidth2 > this.getWidth()) {
				elX = this.getWidth() - elWidth2;
				slX = Math.min(slX, elX - elWidth2 - slWidth2 - LABEL_PADDING);
			}
		} else {
			// TODO Labels too big for screen - should not really happen...
		}

		if (this.thumbDown == START) {
			this.paint.setTextSize(this.labelSizeHighlight);
			canvas.drawText(this.startLabel, slX, this.startLabelY
					- (this.labelSizeHighlight - this.labelSize) / 2,
					this.paint);
			this.paint.setTextSize(this.labelSize);
			canvas.drawText(this.endLabel, elX, this.endLabelY, this.paint);
		} else {
			canvas.drawText(this.startLabel, slX, this.startLabelY, this.paint);
			if (this.thumbDown == END) {
				this.paint.setTextSize(this.labelSizeHighlight);
				canvas.drawText(this.endLabel, elX, this.endLabelY
						- (this.labelSizeHighlight - this.labelSize) / 2,
						this.paint);
				this.paint.setTextSize(this.labelSize);
			} else {
				canvas.drawText(this.endLabel, elX, this.endLabelY, this.paint);
			}
		}

	}

}
