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
 * SeekBar (slider) control with two thumbs for vertical display. Displays
 * labels above the thumbs. Label values are retrieved from the Callback.
 */
public final class VerticalDoubleSeekBar extends DoubleSeekBar {

	public VerticalDoubleSeekBar(final Context context,
			final IDoubleSeekBarCallback callback, boolean lightBackground) {
		super(context, callback, lightBackground);
		Resources res = this.getResources();
		this.startThumbNormal = res
				.getDrawable(R.drawable.seek_thumb_normal_vertical);
		this.startThumbActive = res
				.getDrawable(R.drawable.seek_thumb_pressed_vertical);
		this.endThumbNormal = res
				.getDrawable(R.drawable.seek_thumb_normal_vertical);
		this.endThumbActive = res
				.getDrawable(R.drawable.seek_thumb_pressed_vertical);
		this.startThumb = this.startThumbNormal;
		this.endThumb = this.endThumbNormal;
		this.halfAThumb = this.startThumb.getIntrinsicHeight() / 2;
		this.initialize();
		this.selectionRect.left = this.barPadding;
		this.selectionRect.right = this.barThickness + this.barPadding;
		this.paint.setTextAlign(Align.LEFT);
		backgroundGradient = new LinearGradient(barPadding, 0, barPadding
				+ barThickness / 2, 0, DoubleSeekBar.GREY,
				DoubleSeekBar.DARK_GREY, Shader.TileMode.MIRROR);
		selectionGradient = new LinearGradient(barPadding, 0, barPadding
				+ barThickness / 2, 0, DoubleSeekBar.ORANGE,
				DoubleSeekBar.DARK_ORANGE, Shader.TileMode.MIRROR);

		// this.setStartValue(this.model.getRelativeMinDistance());
		// this.startLabel = this.model.getFormattedMinDistance();
		// this.setEndValue(this.model.getRelativeMaxDistance());
		// this.endLabel = this.model.getFormattedMaxDistance();
		this.startLabelX = this.barThickness + 3 * this.barPadding;
		this.endLabelX = this.barThickness + 3 * this.barPadding;

	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		this.size = h - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(barPadding, 0f, barThickness
				+ barPadding, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected void updateStartBounds() {
		int begin = convertToConcrete(this.getStartValue()) - halfAThumb;
		this.startThumb.setBounds(0, begin,
				this.startThumb.getIntrinsicWidth(), begin
						+ this.startThumb.getIntrinsicHeight());
		this.selectionRect.bottom = begin + halfAThumb;
		this.startLabelY = this.startThumb.getBounds().centerY() + 4;
	}

	protected void updateEndBounds() {
		int begin = convertToConcrete(this.getEndValue()) - halfAThumb;

		this.selectionRect.top = begin + halfAThumb;
		this.endThumb.setBounds(0, begin, this.startThumb.getIntrinsicWidth(),
				begin + this.startThumb.getIntrinsicHeight());
		this.endLabelY = this.endThumb.getBounds().centerY() + 4;
	}

	@Override
	protected int convertToConcrete(final float abstractValue) {

		return Math.round((1 - abstractValue) * this.size) + this.endOffset;

	}

	@Override
	protected float convertToAbstract(final float concreteValue) {
		return 1 - (float) (concreteValue - this.endOffset) / this.size;

	}

	@Override
	protected float getEventCoordinate(final MotionEvent event) {
		return event.getY();
	}

	@Override
	protected void drawPhotoMarks(Canvas canvas) {
		float pos;
		for (float mark : _photoMarks) {
			pos = backgroundRect.top + backgroundRect.height() - endOffset
					- mark * size;
			canvas.drawLine(backgroundRect.left, pos, backgroundRect.right,
					pos, paint);
		}
	}

	@Override
	protected void drawLabels(Canvas canvas) {
		if (this.thumbDown == START) {
			this.paint.setTextSize(this.labelSizeHighlight);
			canvas.drawText(this.startLabel, this.startLabelX, this.startLabelY
					+ (this.labelSizeHighlight - this.labelSize) / 2,
					this.paint);
			this.paint.setTextSize(this.labelSize);
			canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY,
					this.paint);
		} else {
			canvas.drawText(this.startLabel, this.startLabelX,
					this.startLabelY, this.paint);
			if (this.thumbDown == END) {
				this.paint.setTextSize(this.labelSizeHighlight);
				canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY
						+ (this.labelSizeHighlight - this.labelSize) / 2,
						this.paint);
				this.paint.setTextSize(this.labelSize);
			} else {
				canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY,
						this.paint);
			}
		}

	}

}
