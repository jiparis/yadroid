package org.jiagjl;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

import org.jiagjl.drawtext.LabelMaker;
import org.jiagjl.drawtext.MultiLabelMaker;
import org.jiagjl.drawtext.matrix.MatrixTrackingGL;

import org.jiagjl.drawtext.NumericSprite;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

public class ShadowsView extends GLBase {

	SolarInformation solarInformation;

	float[] origen=new float[]{0f,0f};
	
	float[] quad = new float[]{
			-1.0f,-1.0f, 0.0f,
			 1.0f,-1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f		
	};
	
	float texCoords[] = new float[] {			
			0.0f, 1.0f,		// bottom-left	
			1.0f, 1.f,		// bottom-right
			0.0f, 0.0f,		// top-left
			1.0f, 0.0f		// top-right	 
	};
	
	float worldCoords[] = new float[] {
		0.0f, 3.0f,
		3.0f, 3.0f,
		0.0f, 0.0f,
		3.0f, 0.0f
	
	};
	FloatBuffer quadBuff;
	FloatBuffer colBuff;
	FloatBuffer texBuff;
	FloatBuffer worldBuff;
	FloatBuffer shadowsBuff;
	FloatBuffer origenBuff;

	
	float rquad = 0f;     //Rotación que se ha aplicado a la Z en el último drawFrame 
    float rquad_obj = 0f; //Rotación objetivo a alcanzar en la Z en sucesivos drawFrame
	float rquad_aux;      //Valor Z actual del sensor de la brújula 
	float rquad_aux_prev; //Valor Z anterior del sensor de la brújula, para calcular la velocidad de cambio de los valores

	float xrot = 0.0f;
	float yrot = 0.0f;
	float xrot_aux = 0.0f;
	float yrot_aux = 0.0f;

	float rquad_aux_call; 
	float xrot_aux_call = 0.0f;
	float yrot_aux_call = 0.0f;
	
	int compassTex;
	int worldTex;
	
	Bitmap bmp;
	Bitmap world;
	
	Context context;
	
	float lightAmbient[]= { 0.5f, 0.5f, 0.5f, 1.0f }; 	// Ambient Light Values
	float lightDiffuse[]= { 1.0f, 1.0f, 1.0f, 1.0f };	// Diffuse Light Values 
	float lightPosition[]= { 0.0f, 0.0f, 2.0f, 1.0f };	// Light Position 	

	
	LabelMaker mLabels;
	MultiLabelMaker mMLabels;
	NumericSprite mNumericSprite;
	Paint mLabelPaint;
	int mLabelNA;
	int mLabelNE, mLabelSE, mLabelNW, mLabelSW, mLabelDot, mLabellocation,
			mLabelDate, mLabelRotation, mLabelShadow, mLabelHelp, mLabelView,
			mLabelCompass, mLabel3D;
	float[] qm = new float[16];
	
	int paused = 0;
	boolean canPress = false;
	
	SensorListener sl = new SensorListener(){

		public void onAccuracyChanged(int sensor, int accuracy) {				
		}

		public void onSensorChanged(int sensor, float[] values) {
			synchronized (ShadowsView.this.sl) {
				if (paused == 0){
					//Azimuth - z
					rquad_aux_call = values[0];
					//Pitch - x
					xrot_aux_call = values[1];
					//Roll - y
					yrot_aux_call = values[2];
				} else if (paused == 2){
					//Azimuth - z
					rquad_aux_call = values[0];
					//Pitch - x
					xrot_aux_call = 0;
					//Roll - y
					yrot_aux_call = 0;	
				} else if (paused == 3){
					//Azimuth - z
					rquad_aux_call = 0;
					//Pitch - x
					xrot_aux_call = 0;
					//Roll - y
					yrot_aux_call = 0;	
				}


			}
	    }
	};
	
	LocationManager loc_mgr; 
	LocationFinder loc_finder;
	String location;
    
	public ShadowsView(Context c) {
		super(c, 5);
		
		quadBuff = makeFloatBuffer(quad);
		texBuff = makeFloatBuffer(texCoords);
		worldBuff = makeFloatBuffer(worldCoords);
		bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.compass);
		world = BitmapFactory.decodeResource(c.getResources(), R.drawable.world);
		context = c;        

		origenBuff=makeFloatBuffer(origen);

		solarInformation=new SolarInformation();

    	loc_mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
//    	Location loc = null;
//    	if ( loc_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
//        	loc = loc_mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        	if ( loc != null ) {
//    	    	solarInformation.setPosition(loc.getLatitude(), loc.getLongitude());
//    	    	loc_finder = new LocationFinder( loc.getLatitude(), loc.getLongitude() );
//    	    	Log.i("LOCATION", loc.getLatitude() + " - " + loc.getLongitude() );
//        	}
//        	else {
//    			showToast(  new int[]{ R.string.msg_lp_no_location, -1, R.string.msg_lp_default } );
//    			loc_finder = new LocationFinder( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY );
//    	    	Log.i("LOCATION", "No location" );
//        	}
//        	loc_mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0.0f, onLocationChange);
//
//        }
//    	else {
//			showToast(  new int[]{ R.string.msg_lp_disbled, -1, R.string.msg_lp_default } );
//			 = new LocationFinder( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY );
//    	}
    	
	}


    private final LocationListener onLocationChange = new LocationListener() {
    	boolean first = true;
		public void onLocationChanged(Location newlocation) {
			if (newlocation != null) {
	    		double lat = solarInformation.getValue(SolarInformation.LATITUDE_VALUE);
	    		double lon = solarInformation.getValue(SolarInformation.LONGITUDE_VALUE);
				double dist = Utils.distance(lat, lon, newlocation.getLatitude(), newlocation.getLongitude(), 'K');
				if ( dist > 0.1 || first ) {
					solarInformation.setPosition( newlocation.getLatitude(), newlocation.getLongitude() );
					loc_finder.locateName( newlocation.getLatitude(), newlocation.getLongitude(), 10, false);
				}
				else
					showToast(R.string.msg_lp_not_changed);
			}
			else
				showToast( "Hola" );
			loc_mgr.removeUpdates(this);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}; 

	
	public String reverseLocation( double latitude, double longitude ) {
		String res = null;
		List<Address> myList = null;
		try {
			Geocoder myLocation = new Geocoder(context, Locale.getDefault());
			myList = myLocation.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
		}
		if ( myList != null ) {
			Address a = myList.get(0);
			res = a.getLocality();
			if ( res == null )
				res = a.getAdminArea();
			if ( res == null )
				res = a.getCountryName();
			else
				res += ", " + a.getCountryName();
		}
		return res;
	}


	class LocationFinder extends Thread {
		double lat, lon;
		boolean first = true;
		boolean stop = false;
		boolean locate = false;
		boolean show = false;
		int retry = -1;

		public LocationFinder() {
		}
		
		synchronized public void locateName( double latitude, double longitude, int retry, boolean showIfEqual ) {
    		lat = latitude;
    		lon = longitude;
			this.retry = retry;
			show = false;
			locate = true;
		}
		
		public void stopThread() {
			stop = true;
		}
		
		public void run() {
			while ( !stop ) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException ex) {}
				synchronized (this) {
					if ( locate ){
				    	String loc = reverseLocation( lat, lon );
						if ( loc != null ) {
							if ( !loc.equals(location) ) {
								location = loc;
								must_init_labels = true;
//								showToast( new String[] { (String) context.getText( R.string.msg_lp_name_found ), null,  location } );
	//							Log.i("findLocation", location);
							} else if ( show ) {
//								showToast( new String[] { (String) context.getText( R.string.msg_lp_name_found ), null,  location } );
							}
							locate = false;
							show = false;
							showToast( new String[] { (String) context.getText( R.string.msg_lp_name_found ), null,  location } );
						} 
					}
				} 
				try {
					Thread.sleep(4000);
				}
				catch (InterruptedException ex) {}
			}
		}
	}

	private void showToast( int[] resourceIds ) {
		String[] lines = new String[resourceIds.length];
		for (int n = 0; n < resourceIds.length; n++ )
			lines[n] = (resourceIds[n] == -1 ? null : (String) context.getText( resourceIds[n] ));
		showToast(lines);
	}

	private void showToast( int resourceId ) {
		showToast((String) context.getText( resourceId ) );
	}
	
	
	private static final String pad[] = new String [20];
    static {
    	pad[0] = "";
    	for ( int n = 1; n < pad.length; n++ )
    		pad[n] = pad[n-1] + " ";
    }

	private void showToast( String[] lines ){
		int max = 0;
		for (int n = 0; n < lines.length; n++ )
			if ( lines[n] != null && lines[n].length() > max )
				max = lines[n].length();
		String text = "";
		for (int n = 0; n < lines.length; n++ ) {
			if ( lines[n] != null && lines[n].length() > 0 ) {
				int delta = Math.min( (int)Math.round( (max - lines[n].length()) / 2.0f + 0.5), pad.length-1 );
				text += pad[delta] + pad[delta] + lines[n] + (n != lines[n].length() ? "\n" : "" );
			}
			else {
				text += (n != lines.length ? "\n" : "" );
			}
		}
		showToast(text);
	}
	
	private void showToast( final String text ){
		post(new Runnable(){
			@Override
			public void run() {
	        	Toast t = Toast.makeText(context, text, Toast.LENGTH_LONG);
		    	t.show();
			}});
	}
	
	public ShadowsView(Context c, AttributeSet as){
		this(c);
	}

	protected void registerSensors(){
		loc_finder = new LocationFinder();
    	loc_finder.start();
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sl, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_GAME);

        startSearchLocation();
	}
	
	protected void unregisterSensors(){
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(sl);
    	loc_mgr.removeUpdates(onLocationChange);
    	loc_finder.stopThread();
		try {
			loc_finder.join();
		}
		catch (InterruptedException ex) {}
		loc_finder = null;
	}


	protected void startSearchLocation(){
    	loc_mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0.0f, onLocationChange);
	}
	
	@Override
	protected void end(GL10 gl) {
		super.end(gl);		
	}


	@Override
	protected GL10 init(GL10 gl){

		if (!(gl instanceof MatrixTrackingGL) )
			gl = new MatrixTrackingGL(gl);

		gl.glMatrixMode(GL10.GL_MODELVIEW);		

		// Smooth shading
		gl.glShadeModel(GL10.GL_SMOOTH);	// Enables Smooth shading
		
		// Depth buffer
		gl.glClearDepthf(1.0f);		
		// Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST);	// Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL);		// The Type Of Depth Test To Do
		
		// perspective
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);		// Really Nice Perspective Calculations
	
		// Clear color
		gl.glClearColor(0.7f, 0.7f, 0.9f, 0.5f);
		
		// Load textures
		compassTex = loadTexture(gl, bmp);
		worldTex = loadTexture(gl, world);
		
		// lights
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);

		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
		
		must_init_labels = true;
		mMLabels = null;
        initLabels(gl);
       
        return gl;
	}

	boolean must_init_labels = true;
	
	protected void initLabels(GL10 gl){
		if ( !must_init_labels )
			return;
		if ( mMLabels == null ) {
			//Los textos a pintar hay que crearlos primero usando un Paint, 
			//para que se carguen como texturas 2D a las que haremos 
			//referencia después
	        mLabelPaint = new Paint();
	        mLabelPaint.setTextSize(16);
	        mLabelPaint.setAntiAlias(true);
	        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);

			mMLabels = new MultiLabelMaker(true, mLabelPaint);
		}        
        String date = DateFormat.getDateInstance().format(solarInformation.getCalendar().getTime());
        mMLabels.add(new String[] { 
        		date, 
        		(location == null ? "" : location),
				(String) context.getText(R.string.txt_out_of_range),
				(String) context.getText(R.string.txt_rotation_3d),
				(String) context.getText(R.string.txt_rotation_paused),
				(String) context.getText(R.string.txt_rotation_compass),
				(String) context.getText(R.string.txt_rotation_0),
				(String) context.getText(R.string.txt_shadow_length),
				(String) context.getText(R.string.txt_help),
        		(String) context.getText(R.string.txt_view),
        		});
        mLabelDate = 0;
        mLabellocation = 1;
        mLabelNA = 2;
        mLabelRotation = 3;  //3D
        					 //Paused
        					 //Compass
        					 //Not rotated
        mLabelShadow = 7; 
        mLabelHelp = 8;
        mLabelView = 9;

    	must_init_labels = false;
	}
	
	final float DEFAULT_PROP = 0.25f; 
	final float MIN_PROP = 0.125f; 
	final float MAX_PROP = 1f; 

	float touchX, touchY;
	float touchP;

	float propX = DEFAULT_PROP, propY = DEFAULT_PROP;
	float prop_iniX, prop_iniY;

	long event_time;
	
    @Override
	public boolean onTouchEvent( MotionEvent event ) {
    	switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			event_time = System.currentTimeMillis();
    		touchX = event.getX();
    		touchY = event.getY();
    		touchP = (MAX_PROP-MIN_PROP)/(width/4f);
//	    	Log.i("TOUCH", ""+event.getAction() );
			break;

		case MotionEvent.ACTION_MOVE:
			if ( (System.currentTimeMillis()- event_time) < 175 )
			{
	    		touchX = event.getX();
	    		touchY = event.getY();
			} else {
	    		float deltaX = event.getX() - touchX;
	    		if ( Math.abs(deltaX) > 0.5 ) {
	    			propX += deltaX*touchP;
	    			propX = Math.min( propX, MAX_PROP);
	    			propX = Math.max( propX, MIN_PROP);
	        		touchX = event.getX();
	    		}
	    		float deltaY = event.getY() - touchY;
	    		if ( Math.abs(deltaY) > 0.5 ) {
	    			propY += deltaY*touchP;
	    			propY = Math.min( propY, MAX_PROP);
	    			propY = Math.max( propY, MIN_PROP);
	        		touchY = event.getY();
	    		}
//	        	Log.i("TOUCH", ""+event.getAction() + " - " + deltaX + " - " + deltaY + " - " + propX + " - " + propY );
//	        	Log.i("TOUCH", ""+event.getAction() + " - " + event.getX() + " - " + event.getY() );
			}
			break;

		case MotionEvent.ACTION_UP:
			if ( (System.currentTimeMillis()- event_time) < 175 ) {
				paused_time = System.currentTimeMillis();
				paused = (paused+1)%4;
			}
//	    	Log.i("TOUCH", ""+event.getAction() + " - " + (System.currentTimeMillis()- event_time) );
			break;
			
		default:
			break;
		}
		return true;
	}
	
	long time = System.currentTimeMillis();
	long paused_time = System.currentTimeMillis();
	
    @Override
	protected void drawFrame(GL10 gl) {
		initLabels(gl);

		softAngles();

		//float[] sombra=solarInformation.calculateStripShadow(solarInformation.getCalendar());
		float[] sombra=solarInformation.calculateRectangleShadow(solarInformation.getCalendar(),rquad, propX/2, propY/2);
		shadowsBuff=makeFloatBuffer(sombra);

		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
		gl.glMatrixMode(GL10.GL_MODELVIEW);		
		//gl.glColor4f(1.0f,1.0f,1.0f, 0.0f);				// Set The Color
		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glTranslatef(0,0,-5); // center scene

        // mundo
        gl.glPushMatrix();
        	rotate( gl, xrot, yrot, 0 );
		    rotate( gl, 0, 0, rquad );

		    gl.glTranslatef(0f,0f, -0.01f); //un poco por debajo de la brújula
			drawWorld(gl);
		gl.glPopMatrix();
        
		// brújula
		gl.glPushMatrix(); 
			gl.glEnable(GL10.GL_TEXTURE_2D);						// Enable Texture Mapping 
						
		    rotate( gl, xrot, yrot, 0 );
		    rotate( gl, 0, 0, rquad );
			
			// textura a aplicar
			gl.glBindTexture(GL10.GL_TEXTURE_2D, compassTex);
		
			gl.glEnable(GL10.GL_BLEND);
			gl.glColor4f(1, 1, 1, 0.5f);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
			// send vertices to the renderer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, quadBuff);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				
			// send texture coords to the renderer
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuff);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
				
			// draw!
			gl.glNormal3f(0,0,1.0f);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);		
			gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);		

		gl.glPopMatrix();	
		
		
		// poste
		gl.glPushMatrix();
			gl.glLoadIdentity();					// Reset The View, loading the identity matrix
			gl.glTranslatef(0,0,-5); // center scene

		    rotate( gl, xrot, yrot, 0 );
			
			gl.glColor4f(0, 0, 1, 1.0f);
			gl.glTranslatef(0, 0, 0.5f);
			gl.glScalef(propX, propY, 1.0f);
			drawCube(gl);
		gl.glPopMatrix();
	
		// sombra
		gl.glPushMatrix();
		    rotate( gl, xrot, yrot, 0 );

			gl.glTranslatef(0, 0, 0.01f);
			drawShadow(gl);
		gl.glPopMatrix();
		
    	mMLabels.print(gl, (int)mLabelDate, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	mMLabels.print(gl, " - ", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	mMLabels.println(gl, solarInformation.getStringTime(SolarInformation.LOCAL_TIME), MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	
    	if ( location != null )
        	mMLabels.println(gl, mLabellocation, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	else {
    		float lat = Math.round((float)solarInformation.getValue(SolarInformation.LATITUDE_VALUE)*1000f)/1000f;
    		float lon = Math.round((float)solarInformation.getValue(SolarInformation.LONGITUDE_VALUE)*1000f)/1000f;
    		mMLabels.print(gl, lat, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    		mMLabels.print(gl, ", ", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    		mMLabels.println(gl, lon, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	}

    	float shadow = (float)solarInformation.getValue(SolarInformation.SHADOW_LENGTH_VALUE);
    	mMLabels.print(gl, mLabelShadow, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
        if ( shadow > 20.0f )
        	mMLabels.println(gl, mLabelNA, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
        else  
        	mMLabels.println(gl, (float)Math.floor(shadow*1000)/1000f, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
        
    	if ( true || paused != 0 ) {
    		long delta = (System.currentTimeMillis() - paused_time);
    		long mod = delta % 1000;
    		if ( /*delta > 10000 ||*/ mod < 500 || paused == 0 ) {
            	mMLabels.print(gl, mLabelView, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    			mMLabels.println(gl, mLabelRotation+paused, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    		}
    	}
//    	if ( toggleDec > 0 )
//    		mMLabels.println(gl, (long)toggleDec, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );

    	mMLabels.println(gl, mLabelHelp, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_DOWN );
    	mMLabels.println(gl, " ", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_DOWN );
    	mMLabels.println(gl, " ", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_DOWN );
    	mMLabels.println(gl, " ", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_DOWN );

    	mMLabels.flush(gl, mWidth, mHeight);
        
	}

    private void rotate( GL10 gl, float x, float y, float z ) {
		//Obtenemos la matriz de rotación simultanea en los 3 ejes
		Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(x*Utils.TO_RAD_FACTOR), (float)(y*Utils.TO_RAD_FACTOR), (float)(z*Utils.TO_RAD_FACTOR)) );
		//La aplicamos al modelo multiplicandola con OpenGL
		gl.glMultMatrixf(qm, 0);
    }
    
	private void drawWorld(GL10 gl) {
    	gl.glEnable(GL10.GL_TEXTURE_2D);
    	
    	gl.glScalef(4f, 4f, 1.0f);
		// textura a aplicar
		gl.glBindTexture(GL10.GL_TEXTURE_2D, worldTex);
		
    	gl.glColor4f(1f, 1, 1, 1f);

		// send vertices to the renderer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, quadBuff);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			
		// send texture coords to the renderer
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuff);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
			
		// draw!
		gl.glNormal3f(0,0,1.0f);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);		
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	private void drawShadow(GL10 gl) {
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);	
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, shadowsBuff);
		gl.glColor4f(0f,0f,0f, 0.6f);				// Set The Color
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, shadowsBuff.capacity()/2);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
		
		gl.glEnable(GL10.GL_LIGHTING);

	}


	int mWidth;
	int mHeight;
	
	protected void resize(GL10 gl, int w, int h) {
		super.resize(gl, w, h );
		mWidth = w;
		mHeight= h;
	}

	

	static float DEGREES_MIN = 0.0f;
	static float DEGREES_MAX = 70.0f;
	static float FX_MIN = 0.0f;
	static float FX_MAX = 2.0f;
	static float DEGREES_TO_FX_FACTOR = Utils.getFactor( DEGREES_MIN, DEGREES_MAX, FX_MIN, FX_MAX ); 
	
	static float FX_RES_MIN = fx(FX_MIN);
	static float FX_RES_MAX = fx(FX_MAX);
	static float FX_RES_TO_DEGREES_FACTOR = Utils.getFactor( FX_RES_MIN, FX_RES_MAX, DEGREES_MIN, DEGREES_MAX ); 


	//Mapea los grados para que sigan una progresión exponencial en lugar de lineal
	static public float softenDegrees( float angle ) {
		return Utils.scaleFactor( fx( Utils.scaleFactor( angle, DEGREES_TO_FX_FACTOR, DEGREES_MIN, FX_MIN, FX_MAX )), FX_RES_TO_DEGREES_FACTOR, FX_RES_MIN, DEGREES_MIN, DEGREES_MAX );
	}

	static public float fx( float value ) {
		return (float)Math.exp(value);
	}

	public boolean toggleX = true;
	public int toggleDec = 1;
	
	float[][] rquad_window = new float[10][2];
	{ 
		rquad_window[0][0] = Float.NEGATIVE_INFINITY;
		rquad_window[0][1] =  (float)System.currentTimeMillis();
	}
	int window_size = rquad_window.length;
	int window_first = 0;
	int window_last = -1;
	
	
	private void softAngles() {
		synchronized (sl) {
			//Azimuth - z
			rquad_aux = rquad_aux_call ;
			//Pitch - x
			xrot_aux = xrot_aux_call;
			//Roll - y
			yrot_aux = yrot_aux_call;	
		}
		int wl_aux = (window_last+1)%window_size;
		if ( window_first == wl_aux && window_last >= 0 )
			window_first = (window_first+1)%window_size;
		window_last = wl_aux;
		
		rquad_window[window_last][0] = rquad_aux;
		rquad_window[window_last][1] = System.currentTimeMillis();
		
		float delta = Math.abs(rquad_window[window_last][0]-rquad_window[window_first][0]);
		if ( delta > 180 ) {
			delta = 360-delta;
		}
		
		float speed = delta / (rquad_window[window_last][1]-rquad_window[window_first][1]+1);

//		float delta_log = delta;
//			
//		boolean discarded = true;
		if ( (speed < 75f && delta != 0) || paused == 3 ) {
			rquad_obj = (float)rquad_aux;
//			discarded = false;
			if ( toggleDec == 1 && paused != 3 )
				rquad_obj -= solarInformation.getValue(SolarInformation.DECLINATION_VALUE);
			if ( toggleDec == 2  && paused != 3 )
				rquad_obj += solarInformation.getValue(SolarInformation.DECLINATION_VALUE);
		}
		time = System.currentTimeMillis();
		rquad_aux_prev = rquad_aux;

		float inc = 1;
		float rquad_t = rquad + (360 - rquad_obj);
		if ( rquad_t > 360 )
			rquad_t -= 360;
		float sign = -1;
		delta = rquad_t;
		if ( rquad_t >= 180 ) {
			sign = 1;
			delta = 360-rquad_t;
		}
			
		if ( delta <= 1 )
			inc = sign * delta;
		else if ( delta <= 5 )
			inc = 1 * sign;
		else if ( delta <= 20 )
			inc = 5 * sign;
		else if ( delta <= 50 )
			inc = 10 * sign;
		else
			inc = 15 * sign;

		rquad += inc;
		if ( rquad < 0 )
			rquad += 360;
		else if ( rquad >= 360 )
			rquad -= 360;
		rquad = Math.round(rquad);
//		Log.i( "ROTACIÓN", rquad_obj + " - " + speed + " - " + delta_raw + " - " + delta + " - " + rquad_log + " - " + rquad + " - " + inc );
//		Log.i( "ROTACIÓN", speed + " - " + rquad_obj + " - " + rquad_aux + " - " + delta_raw + " - " + (discarded?"DISCARDED":"")  );
//		Log.i("ROTACIÓN", 
//				 "s:"  + r(speed) + 
//				" o:" + r(rquad_obj) + 
//				" l:"  + r(rquad_window[window_last][0]) + 
//				" f:"  + r(rquad_window[window_first][0]) + 
//				" d:"  + r(delta_log) + 
//				" do:"  + r(delta*sign) + 
//				" - "   + (discarded ? "DISCARDED" : ""));

		//Inclinamos la x un poco, porque es la forma natural del
		//teléfono en la mano
		//                 JI         Otro
		xrot = (toggleX?xrot_aux:-2*xrot_aux);
		yrot = yrot_aux*2;
//			xrot += 30.0f;
		if ( xrot > 70.0f )
			xrot = 70.0f;
		else if ( xrot < -70.0f )
			xrot = -70.0f;
		
		if ( yrot > 70.0f )
			yrot = 70.0f;
		else if ( yrot < -70.0f )
			yrot = -70.0f;
		
		//Aplicamos una conversión a los grados de rotación x e y 
		//a través de una curva exponencial para que la inclinación
		//no sea lineal
		//No se si sirve para algo pero me parecía interesante hacerlo ;-)
		xrot = softenDegrees(Math.abs(xrot))*Math.signum(xrot);
		yrot = softenDegrees(Math.abs(yrot))*Math.signum(yrot);

		if ( paused < 2 )
			xrot = (toggleX?xrot-10f:xrot-20f);
		
//		Log.i("", xrot + " - " + yrot + " - " + rquad  );
	}
	
//	static private String r( float v ) {
//		
//		char[] cad = "0000.000".toCharArray();
//		String number = ""+v;
//		int pos = number.indexOf('.');
//		for ( int n = pos-1; n>=0; n-- )
//			cad[4-pos+n] = number.charAt(n);
//		for ( int n = pos+1; (4+n-pos) < cad.length && n < number.length(); n++ )
//			cad[4+n-pos] = number.charAt(n);
//		return new String(cad);
//	}
}
