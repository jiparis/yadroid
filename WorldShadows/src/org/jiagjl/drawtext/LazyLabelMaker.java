package org.jiagjl.drawtext;


import javax.microedition.khronos.opengles.GL10;

import android.graphics.Paint;
import android.util.Log;

public class LazyLabelMaker {

	LabelMaker lm; 
	String[] mLabels;
	float[] mRawWidth;
    Paint mPaint;
    int[] mLabelId;
    float[] mWidth; 
    float[] mHeight; 
    float maxHeight;
    int size;
    
    private boolean must_reinitialize = true;
    int strikeWidth; 
    int strikeHeight;
    boolean fullColor;
    float maxTextSize;
    
    public LazyLabelMaker(boolean fullColor, Paint paint) {
    	this.fullColor = fullColor;
		mPaint = paint;
    }

    public void add( String[] labels ) {
		mLabels = labels;
		int new_size = mLabels.length;
		if ( new_size != size ) {
			mLabelId = new int[new_size];
			mWidth = new float[new_size];
			mHeight = new float[new_size];
			mRawWidth = new float[new_size];
		}
		size = new_size;
		maxTextSize = 0;
		for ( int n = 0; n < size; n++ ) {
			if ( mLabels[n] == null )
				mLabels[n] = "null";
			mRawWidth[n] = (float)Math.ceil(mPaint.measureText(mLabels[n]));
			if ( mRawWidth[n] > maxTextSize )
				maxTextSize = mRawWidth[n];
		}
		must_reinitialize = true;
    }

	public void update( int index, String label ) {
		mLabels[index] = label;
		must_reinitialize = true;
	}
	
    public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
    	lm.beginDrawing(gl, viewWidth, viewHeight);
    }
	
    public void endDrawing(GL10 gl) {
    	lm.endDrawing(gl);
    }
	
    public void draw(GL10 gl, float x, float y, int z, int index) {
    	reinitialize(gl);
    	lm.draw(gl, x, y, z, mLabelId[index]);
	}
	
	protected void reinitialize( GL10 gl ) {
		if ( !must_reinitialize )
			return;
		Log.i("reinitialize", "reinitialize");
		int newStrikeWidth = roundUpPower2((int)maxTextSize);
		int newStrikeHeight = size * roundUpPower2((int) mPaint.getFontSpacing());
		if ( lm == null ) {
			strikeWidth = newStrikeWidth;
			strikeHeight = newStrikeHeight;
	    	lm = new LabelMaker(fullColor, strikeWidth, strikeHeight);
		} else if ( strikeWidth != newStrikeWidth || strikeHeight != newStrikeHeight ) {
			lm.shutdown(gl);
			strikeWidth = newStrikeWidth;
			strikeHeight = newStrikeHeight;
	    	lm = new LabelMaker(fullColor, strikeWidth, strikeHeight);
		} else
			lm.shutdown(gl);
		
		lm.initialize(gl);
		lm.beginAdding(gl);
		for ( int n = 0; n < size; n++ ) {
			mLabelId[n] = lm.add( gl, mLabels[n], mPaint );
			mWidth[n] = lm.getWidth(mLabelId[n]);
			mHeight[n] = lm.getHeight(mLabelId[n]);
			if ( mHeight[n] > maxHeight )
				maxHeight = mHeight[n];
		}
		lm.endAdding(gl);
        must_reinitialize = false;
	}

    /**
     * Find the smallest power of two >= the input value.
     * (Doesn't work for negative numbers.)
     */
    private int roundUpPower2(int x) {
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >>16);
        return x + 1;
    }


}
