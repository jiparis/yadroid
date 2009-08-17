package org.jiagjl.drawtext;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Paint;

public class CharSeqLabelMaker extends LazyLabelMaker {

	static public final int NUMBERS_LM = 0;
	static public final int ALFABET_LM = 1;
	static public final int COMPLETE_LM = 2;
	
	static public final String NUMBERS_SEQ = buildSequence('0', '9');
	static public final String LOWERCASE_SEQ = buildSequence('a', 'z');
	static public final String UPPERCASE_SEQ = buildSequence('A', 'Z');
	static public final String ALFABET_SEQ = buildSequence('A', 'z');
	static public final String COMPLETE_SEQ = buildSequence(' ', '~');

	static public String buildSequence( char start, char end ) {
		char[] sb = new char[end-start+1];
		for ( int n = start; n <= end; n++ )
			sb[n-start] = (char)n;
		return String.copyValueOf(sb);
	}

	public interface PositionFinder {
		int getPosition( char c );
	}
		
	
	char first;
	PositionFinder mFinder;

	public CharSeqLabelMaker( char start, char end, boolean fullColor, Paint paint ) {
		this( buildSequence(start, end), fullColor, paint );
	}
	
	public CharSeqLabelMaker( char start, char end, PositionFinder finder, boolean fullColor, Paint paint ) {
		this( buildSequence(start, end), finder, fullColor, paint );
	}
	
	public CharSeqLabelMaker( final CharSequence seq, boolean fullColor, Paint paint ) {
		this(seq, new PositionFinder(){

			char first = seq.charAt(0);
			int size = seq.length();
			
			@Override
			public int getPosition(char c) {
		        int pos = c - first;
		        if ( pos < 0 || pos >= size )
		        	pos = -1;
		        return pos;
			}}, fullColor, paint );
	}

	public CharSeqLabelMaker( CharSequence seq, PositionFinder finder, boolean fullColor, Paint paint ) {
		super( fullColor, paint );
		mFinder = finder; 
		String[] chars = new String[seq.length()];
		for( int n = 0; n < seq.length(); n++ )
			chars[n] = String.valueOf(seq.charAt(n));
		add( chars );
	}

    public void draw(GL10 gl, float x, float y, int z, String text) {
    	reinitialize(gl);

    	int length = text.length();
        for(int i = 0; i < length; i++) {
            int index = mFinder.getPosition(text.charAt(i));
            if ( index >= 0 ) {
	        	lm.draw(gl, x, y, z, mLabelId[index]);
	            x += mWidth[index];
            }
        }
    }

    public float getWidth(GL10 gl, String text) {
    	reinitialize(gl);

    	float width = 0.0f;
        int length = text.length();
        for(int i = 0; i < length; i++) {
            width += mWidth[mFinder.getPosition(text.charAt(i))];
        }
        return width;
    }

    public float getHeight(GL10 gl, String text) {
    	reinitialize(gl);
    	
    	return maxHeight;
    }
}
