package org.andamobile.ashadow.drawtext;

import javax.microedition.khronos.opengles.GL10;

import org.andamobile.ashadow.drawtext.CharSeqLabelMaker.PositionFinder;

import android.graphics.Paint;

public class LabelPainter extends LazyLabelMaker {

	public LabelPainter( boolean fullColor, Paint paint ) {
		super( fullColor, paint );
		
	}

	CharSeqLabelMaker textLM;
	CharSeqLabelMaker longLM;
	CharSeqLabelMaker floatLM;
	
	public LabelMaker getIndexLM() {
		return lm;
	}
	
	public CharSeqLabelMaker getTextLM() {
		initTextLM();
		return textLM;
	}
	
	public CharSeqLabelMaker getLongLM() {
		initLongLM();
		return longLM;
	}
	
	public CharSeqLabelMaker getFloatLM() {
		initFloatLM();
		return floatLM;
	}
	
	public void draw(GL10 gl, float x, float y, int z, int index) {
		super.draw(gl, x, y, z, index);
	}
	
	public void draw(GL10 gl, float x, float y, int z, String text) {
		getTextLM().draw(gl, x, y, z, text);
	}
	
	public void draw(GL10 gl, float x, float y, int z, long number ) {
		getLongLM().draw(gl, x, y, z, String.valueOf(number));
	}
	
	public void draw(GL10 gl, float x, float y, int z, float number ) {
		getFloatLM().draw(gl, x, y, z, String.valueOf(number));
	}

	public float getWidth(GL10 gl, int index) {
		return getIndexLM().getWidth(index);
	}
	
	public float getWidth(GL10 gl, String text) {
		return getTextLM().getWidth(gl, text);
	}
	
	public float getWidth(GL10 gl, long number ) {
		return getLongLM().getWidth(gl, String.valueOf(number));
	}
	
	public float getWidth(GL10 gl, float number ) {
		return getFloatLM().getWidth(gl, String.valueOf(number));
	}
	
	private void initTextLM() {
		if ( textLM == null ) {
			String seq = CharSeqLabelMaker.buildSequence(' ', '9');//CharSeqLabelMaker.COMPLETE_SEQ;
//			String extra = "¡…Õ”⁄·ÈÌÛ˙Ò—";
			String extra = ":";
			textLM = new CharSeqLabelMaker( seq+extra, new ExtraFinder(seq, extra), fullColor, mPaint );
		}
	}
	
	private void initLongLM() {
		if ( longLM == null ) {
			String seq = CharSeqLabelMaker.NUMBERS_SEQ;
			String extra = "-";
			longLM = new CharSeqLabelMaker( seq+extra, new ExtraFinder(seq, extra), fullColor, mPaint );
		}
	}
	
	public void initFloatLM() {
		if ( floatLM == null ) {
			String seq = CharSeqLabelMaker.NUMBERS_SEQ;
			String extra = ".-";
			floatLM = new CharSeqLabelMaker( seq+extra, new ExtraFinder(seq, extra), fullColor, mPaint );
		}
	}

	class ExtraFinder implements PositionFinder {

		int size;
		char first;
		char[] mExtra;
		
		public ExtraFinder ( String seq, String extra ) {
			size = seq.length();
			first = seq.charAt(0);
			mExtra = extra.toCharArray();
		}
		
		
		public int getPosition(char c) {
			int pos = c-first;
			if ( pos < 0 || pos >= size ) {
				pos = -1;
				for( int n = mExtra.length-1; n >= 0; n-- )
					if ( mExtra[n] == c)
						pos = size+n;
			}
			return pos;
		}
	}
}
