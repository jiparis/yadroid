package org.jiagjl.drawtext;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Paint;

public class MultiLabelMaker extends LabelPainter {
//	int size;
//    float[] mWidth; 
//    float[] mHeight; 
//    float maxH;
//    int[] mLabelId;

//    float viewHeightR;
    
//	Vector<Long> vl = new Vector<Long>();
//	Vector<Float> vf = new Vector<Float>();
//	Vector<String> vt = new Vector<String>();
	long[] vl = new long[10];
	float[] vf = new float[10];
	String[] vt = new String[10];
	int vl_pos = 0;
	int vf_pos = 0;
	int vt_pos = 0;
	
    
	public MultiLabelMaker(boolean fullColor, Paint paint ) {
		super( fullColor, paint );
		Arrays.fill( l[0], -1 );
		Arrays.fill( l[1], -1 );
		Arrays.fill( l[2], -1 );
	}

//	public void add( String[] labels ) {
//		int new_size = labels.length;
//		if ( new_size != size ) {
//			mWidth = new float[new_size];
//			mHeight = new float[new_size];
//			mLabelId = new int[new_size];
//		}
//		super.add(labels);
//	}

	static public final int MAX_LINES = 10;
	static public final int MAX_COMMANDS = 5;
	
	int[][][][] commands = new int[3][3][MAX_LINES][MAX_COMMANDS]; //hAlign - vAlign - line - command
	int[][] l = new int[3][3]; //Número de la última línea. -1 es que no hay ninguna linea
	int[][][] p = new int[3][3][MAX_LINES]; //Posición dentro de la línea
	float[][][] w = new float[3][3][MAX_LINES]; //Ancho de cada línea

	static final public int NEW_LINE = -2;
	static final public int WHITE_SPACE = -3;
	static final public float WHITE_SPACE_WIDTH = 10;

	static final public int VA_TOP = 0;
	static final public int VA_CENTER = 1;
	static final public int VA_DOWN = 2;

	static final public int HA_LEFT = 0;
	static final public int HA_CENTER = 1;
	static final public int HA_RIGHT = 2;
	
	public void print( GL10 gl, int index ) {
		print( gl, index, HA_CENTER, VA_TOP);
    }

	private final int INDEX_TYPE = 0x100;
	private final int TEXT_TYPE = 0x200;
	private final int LONG_TYPE = 0x300;
	private final int FLOAT_TYPE = 0x400;
	private final int TYPE_MASK = 0x700;
	private final int CMD_MASK = 0x0FF;
	
	public void print( GL10 gl, int index, int hAlign, int vAlign ) {
		reinitialize(gl);
		print( gl, INDEX_TYPE, index, hAlign, vAlign );
    }

	public void print( GL10 gl, long number, int hAlign, int vAlign ) {
		getLongLM().reinitialize(gl);
		vl[vl_pos++] = number;
		print( gl, LONG_TYPE, vl_pos-1, hAlign, vAlign );
    }

	public void print( GL10 gl, float number, int hAlign, int vAlign ) {
		getFloatLM().reinitialize(gl);
		vf[vf_pos++] = number;
		print( gl, FLOAT_TYPE, vf_pos-1, hAlign, vAlign );
    }

	public void print( GL10 gl, String text, int hAlign, int vAlign ) {
		getTextLM().reinitialize(gl);
		int pos;
		String next = "";
		do {
			pos = text.indexOf('\n');
			if ( pos != -1 ) {
				next = text.substring( pos+1 );
				text = text.substring( 0, pos );
			}
			vt[vt_pos++] = text;
			print( gl, TEXT_TYPE, vt_pos-1, hAlign, vAlign );
			if ( pos != -1 )
				print( gl, NEW_LINE, hAlign, vAlign );
			text = next;
		} while (pos!=-1); 
    }

	private void print( GL10 gl, int type, int index, int hAlign, int vAlign ) {
		reinitialize(gl);
    	int[][] c = commands[hAlign][vAlign];
    	if ( l[hAlign][vAlign] == -1 ) {
    		l[hAlign][vAlign] = 0;
    		Arrays.fill( w[hAlign][vAlign], 0.0f );
    		Arrays.fill( p[hAlign][vAlign], 0 );
    	}
    	if ( index == NEW_LINE )
    		++l[hAlign][vAlign];
    	else {
    		int line = l[hAlign][vAlign];
    		w[hAlign][vAlign][line] += (index == WHITE_SPACE ? WHITE_SPACE_WIDTH : getWidth(gl, type, index));
    		c[line][p[hAlign][vAlign][line]++] = (index-WHITE_SPACE) | type;
    	}
    }

	public void println( GL10 gl, int index, int hAlign, int vAlign ) {
		print( gl, index, hAlign, vAlign );
		print( gl, NEW_LINE, hAlign, vAlign );
    }	

	public void println( GL10 gl, float number, int hAlign, int vAlign ) {
		print( gl, number, hAlign, vAlign );
		print( gl, NEW_LINE, hAlign, vAlign );
    }	

	public void println( GL10 gl, long number, int hAlign, int vAlign ) {
		print( gl, number, hAlign, vAlign );
		print( gl, NEW_LINE, hAlign, vAlign );
    }	

	public void println( GL10 gl, String text, int hAlign, int vAlign ) {
		print( gl, text, hAlign, vAlign );
		print( gl, NEW_LINE, hAlign, vAlign );
    }	

    public void println( GL10 gl, int index ) {
		print( gl, index );
		print( gl, NEW_LINE );
    }
    
    float[][] mw = {
    	// W      w  
    	{ 0.0f,  0.0f }, // LEFT
    	{ 0.5f, -0.5f }, // CENTER
    	{ 1.0f, -1.0f }, // RIGHT
    };

    float[][] mh = {
    	// H     h
    	{ 1.0f, 0.0f }, // TOP
    	{ 0.5f, 0.5f }, // CENTER
    	{ 0.0f, 1.0f }, // DOWN
    };

    public void flush( GL10 gl, int viewWidth, int viewHeight ) {
//		float viewHeightR = viewHeight - maxHeight;
    	beginDrawing(gl, viewWidth, viewHeight);
    	for( int n = 0; n < 3; n++ )
        	for( int m = 0; m < 3; m++ )
        		if (l[n][m] != -1 ) {
        			float x1 = viewWidth * mw[n][0];
        			float y = viewHeight * mh[m][0] + ((l[n][m]+1)*maxHeight)*mh[m][1];
        			for( int z = 0; z <= l[n][m]; z++ ) {
        				float x = x1 + w[n][m][z]*mw[n][1];
        				y -= maxHeight;
	        			for( int c = 0; c < p[n][m][z]; c++ ) {
	        				int command = commands[n][m][z][c];
	        				int type = command & TYPE_MASK;
	        				command = (command & CMD_MASK) + WHITE_SPACE;
	        				if ( command == WHITE_SPACE )
	        					x += WHITE_SPACE_WIDTH;
	        				else {
	        					x += draw(gl, x, y, 0, type, command);
	        				}
		            	}
	            	}
        			l[n][m] = -1;
        		}
    	endDrawing(gl);
//		Arrays.fill( l[0], -1 );
//		Arrays.fill( l[1], -1 );
//		Arrays.fill( l[2], -1 );
    	vf_pos = 0;
    	vl_pos = 0;
    	vt_pos = 0;
//		vf.clear();
//		vl.clear();
//		vt.clear();
    }

    public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
    	getIndexLM().beginDrawing(gl, viewWidth, viewHeight);
    	if ( longLM != null )
    		getLongLM().beginDrawing(gl, viewWidth, viewHeight);
    	if ( floatLM != null )
    		getFloatLM().beginDrawing(gl, viewWidth, viewHeight);
    	if ( textLM != null )
    		getTextLM().beginDrawing(gl, viewWidth, viewHeight);
    }
    
    public void endDrawing(GL10 gl) {
    	getIndexLM().endDrawing(gl);
    	if ( longLM != null )
    		getLongLM().endDrawing(gl);
    	if ( floatLM != null )
    		getFloatLM().endDrawing(gl);
    	if ( textLM != null )
    		getTextLM().endDrawing(gl);
    }
    
	private float draw(GL10 gl, float x, float y, int z, int type, int command) {
		float res = 0;
		switch (type) {
		case INDEX_TYPE:
			getIndexLM().draw(gl, x, y, mLabelId[command]);
			res = mWidth[command];
			break;
		case LONG_TYPE:
			long valuel = vl[command];
			draw( gl, x, y, 0, valuel );
			res = getWidth(gl, valuel);
			break;
		case FLOAT_TYPE:
			float valuef = vf[command];
			draw( gl, x, y, 0, valuef );
			res = getWidth(gl, valuef);
			break;
		case TEXT_TYPE:
			String valuet = vt[command];
			draw( gl, x, y, 0, valuet );
			res = getWidth(gl, valuet);
			break;
		default:
			break;
		}
		return res;
	}

	private float getWidth(GL10 gl, int type, int command) {
		float res = 0;
		switch (type) {
		case INDEX_TYPE:
			res = mWidth[command];
			break;
		case LONG_TYPE:
			long valuel = vl[command];
			res = getWidth(gl, valuel);
			break;
		case FLOAT_TYPE:
			float valuef = vf[command];
			res = getWidth(gl, valuef);
			break;
		case TEXT_TYPE:
			String valuet = vt[command];
			res = getWidth(gl, valuet);
			break;
		default:
			break;
		}
		return res;
	}
    
}
