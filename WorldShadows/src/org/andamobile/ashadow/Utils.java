package org.andamobile.ashadow;

import javax.microedition.khronos.opengles.GL10;

import org.andamobile.ashadow.drawtext.matrix.MatrixTrackingGL;

import android.util.Log;

public class Utils {

	static final double TO_RAD_FACTOR = Math.PI/180.0;
	static final double TO_DEG_FACTOR = 180.0/Math.PI;
	
	static public float normDegrees( float degrees ) {
		degrees %= 360;
		if ( degrees < 0 )
       		degrees += 360;
        return degrees;
	}

	static public float limitDegrees( float degrees, float limit ) {
        return limitDegrees( degrees, -limit, limit);
	}
	
	static public float limitDegrees( float degrees, float min, float max ) {
		degrees = Math.max( degrees , min );
		degrees = Math.min( degrees , max );
        return degrees;
	}

	static public double distance(double lat1, double lon1, double lat2, double lon2,
			char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	static public double deg2rad(double deg) {
		return (deg * TO_RAD_FACTOR);
	}

	static public double rad2deg(double rad) {
		return (rad * TO_RAD_FACTOR);
	}	
		
	static public float scaleFactor( float value, float factor, float from_min, float to_min, float to_max ) {
		float res = factor * (value-from_min) + to_min;
		if ( res < to_min )
			res = to_min;
		if ( res > to_max )
			res = to_max;
		return res;
	}

	static public float scale( float value, float from_min, float from_max, float to_min, float to_max ) {
		float res = getFactor(from_min, from_max, to_min, to_max) * (value-from_min) + to_min;
		if ( res < to_min )
			res = to_min;
		if ( res > to_max )
			res = to_max;
		return res;
	}

	static public float getFactor( float from_min, float from_max, float to_min, float to_max) {
		return (to_max - to_min) / (from_max - from_min);
	}

	static public void dumpMatrix( float[] matrix, int offset, String title ) {
		Log.i("DUMP MATRIX", title);
		for ( int y = 0; y < 4; y++ ) {
			String res = "";
			for ( int n = 0; n < 4; n++ ) {
				int pos = y+n*4;
				String aux = " [" + (pos<10?"0"+pos:pos)+ "] " + (matrix[pos+offset]<0?"":" ")+ Math.round(matrix[pos+offset]*1000)/1000.0;
				if ( aux.length() == 10 )
					aux += "  ";
				else if ( aux.length() == 11 )
					aux += " ";
				res += aux;
			}			
			Log.i("DUMP MATRIX", res);
		}
	}

	
	static public class Quat {
	    float w, x, y, z;
	    
	    Quat() {
	    	this( 0, 0, 0, 0 );
	    }
	    
	    Quat( float w, float x, float y, float z ) {
	    	this.w = w;
	    	this.x = x;
	    	this.y = y;
	    	this.z = z;
	    }
	}
	
	static public Quat quatMult( Quat q1, Quat q2 ) {
		Quat res = new Quat();
		res.w = q1.w*q2.w;
		res.x = q1.w*q2.x + q1.x*q2.w + q1.y*q2.z - q1.z*q2.y;
		res.y = q1.w*q2.y + q1.y*q2.w + q1.z*q2.x - q1.x*q2.z;
		res.z = q1.w*q2.z + q1.z*q2.w + q1.x*q2.y - q1.y*q2.x;
		return res;
	}
	
	static public Quat eulerToQuat( float x, float y, float z ) {
		Quat qx = new Quat( (float)Math.cos(x/2), (float)Math.sin(x/2), 0, 0 );
		Quat qy = new Quat( (float)Math.cos(y/2), 0, (float)Math.sin(y/2), 0 );
		Quat qz = new Quat( (float)Math.cos(z/2), 0, 0, (float)Math.sin(z/2) );
		return quatMult( quatMult( qx, qy ), qz );
	}

	static public void quatToMatrix( float[] res, int offset, Quat q ) {
		res[offset+0] = 1 - 2*q.y*q.y-2*q.z*q.z;
		res[offset+1] = 2*q.x*q.y + 2*q.w*q.z;
		res[offset+2] = 2*q.x*q.z - 2*q.w*q.y;
		res[offset+3] = 0;
		res[offset+4] = 2*q.x*q.y - 2*q.w*q.z;
		res[offset+5] = 1 - 2*q.x*q.x-2*q.z*q.z;
		res[offset+6] = 2*q.y*q.z + 2*q.w*q.x;
		res[offset+7] = 0;
		res[offset+8] = 2*q.x*q.z + 2*q.w*q.y;
		res[offset+9] = 2*q.y*q.z - 2*q.w*q.x;
		res[offset+10] = 1 - 2*q.x*q.x-2*q.y*q.y;
		res[offset+11] = 0;
		res[offset+12] = 0;
		res[offset+13] = 0;
		res[offset+14] = 0;
		res[offset+15] = 1.0f;
	}	
	
	
	//No finalizada / No usada
	static public void rotate( MatrixTrackingGL gl, float yaw, float pitch, float roll ) {
		float[] matrix = new float[32];

		gl.glMatrixMode(GL10.GL_MODELVIEW);		
		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.getMatrix(matrix, 0);
		dumpMatrix( matrix, 0, "Identity" );
		
		gl.glRotatef(yaw, 1.0f, 0, 0);
		gl.getMatrix(matrix, 0);
		dumpMatrix( matrix, 0, "Rotacion Yaw - X" );
		
		
//		((GL11)gl.mgl).glGetFloatv(GL11.GL_MODELVIEW_MATRIX, matrix, 0);
//		for ( int n = 0; n < 16; n++ )
//			Log.i("MATRIX YAW1 "+n, ""+matrix[n]);

		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glRotatef(pitch, 0, 1.0f, 0);
		gl.getMatrix(matrix, 0);
		dumpMatrix( matrix, 0, "Rotacion Pitch - Y" );

		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glRotatef(roll, 0, 0, 1.0f);
		gl.getMatrix(matrix, 0);
		dumpMatrix( matrix, 0, "Rotacion Roll - Z" );
	}
	
    static long time[] = new long[10];
    static {
    	time[0] = System.currentTimeMillis(); 
    }
    static int t_pos = 0;
    static String[] pad = new String[10];
    static {
    	pad[0] = "";
    	for ( int n = 1; n < pad.length; n++ )
    		pad[n] = pad[n-1] + " ";
    }
    
    static public void startLogTime(int log, String where) {
    	long t = System.currentTimeMillis();
        time[log] = t;
        Log.i("TIME_START_" + log, pad[log] + where );
    }

    static public void logTime(int log, String where) {
    	long t = System.currentTimeMillis();
        Log.i("TIME_" + log, pad[log] + where + " - " + (t-time[log]) );
        time[log] = t;
    }

    static public void pushTime(String where) {
    	++t_pos;
    	startLogTime(t_pos, where);
    }
    
    static public void popTime(String where) {
        logTime(t_pos, where);
    	--t_pos;
    }
    

}
