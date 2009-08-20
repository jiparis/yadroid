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
import android.location.LocationManager;
import android.util.AttributeSet;
import android.util.Log;

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
	int mLabelNE, mLabelSE, mLabelNW, mLabelSW, mLabelDot, mLabellocation, mLabelDate, mLabelRotation, mLabelShadow;
	float[] qm = new float[16];
	
	int paused = 0;
	boolean canPress = false;
	
	SensorListener sl = new SensorListener(){

		public void onAccuracyChanged(int sensor, int accuracy) {				
		}

		public void onSensorChanged(int sensor, float[] values) {
			synchronized (ShadowsView.this) {
				if (paused == 0){
					//Azimuth - z
					rquad_aux = values[0];
					//Pitch - x
					xrot_aux = values[1];
					//Roll - y
					yrot_aux = values[2];
				} else if (paused == 2){
					//Azimuth - z
					rquad_aux = 0;
					//Pitch - x
					xrot_aux = 0;
					//Roll - y
					yrot_aux = 0;	
				}

			}
	    }
	};
	
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

    	LocationManager loc_mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    	Location loc = null;
    	if ( loc_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
        	loc = loc_mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	} 
//    	else 
//    	if ( loc_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
//        	loc = loc_mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }
    	if ( loc != null ) {
	    	solarInformation.setPosition(loc.getLatitude(), loc.getLongitude());
	    	Log.i("LOCATION", loc.getLatitude() + " - " + loc.getLongitude() );
    	}

    	findLocation();
	}

	public void findLocation() {
	    try {
			Geocoder myLocation = new Geocoder(context, Locale.getDefault());   
			List<Address> myList = myLocation.getFromLocation(SolarInformation.DEFAULT_LATITUDE, SolarInformation.DEFAULT_LONGITUDE, 1);
			Address a = myList.get(0);
			location = a.getLocality();
			if ( location == null )
				location = a.getAdminArea();
			if ( location == null )
				location = a.getCountryName();
			else
				location += ", " + a.getCountryName();
			if ( location != null ) {
				Log.i("findLocation", location);
				must_init_labels = true;
			}
		} catch (IOException e) {
			Log.e("Location", "Error", e);
		}
	}
	
	public ShadowsView(Context c, AttributeSet as){
		this(c);
	}
	
	protected void registerSensors(){
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sl, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_GAME);
	}
	
	protected void unregisterSensors(){
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(sl);
	}

	@Override
	protected void end(GL10 gl) {
		super.end(gl);		
	}



	@Override
	protected GL10 init(GL10 gl){

		MatrixTrackingGL mgl = new MatrixTrackingGL(gl);
		gl = (GL10)mgl;

		// Smooth shading
		gl.glShadeModel(GL10.GL_SMOOTH);	// Enables Smooth shading
		
		// Depth buffer
		gl.glClearDepthf(1.0f);				// Depth Buffer Setup
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
		
        initLabels(gl);
       
        return mgl;
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
				(String) context.getText(R.string.txt_rotation),
				(String) context.getText(R.string.txt_rotation_0),
				(String) context.getText(R.string.txt_shadow_length) });
        mLabelDate = 0;
        mLabellocation = 1;
        mLabelNA = 2;
        mLabelRotation = 3;
        mLabelShadow = 5;

    	must_init_labels = false;
	}
	
	
	int frames = 0;
	long time = System.currentTimeMillis();
	long paused_time = System.currentTimeMillis();
	
    @Override
	synchronized protected void drawFrame(GL10 gl) {
		if ( location == null && frames++ > 100 ) {
			findLocation();
			frames = 0;
		}
		initLabels(gl);
		if (isPressed() && canPress){			
			paused = (paused+1) % 3;
			if ( paused == 1)
				paused_time = System.currentTimeMillis();
			canPress = false;
		}		
		if (!isPressed())
			canPress = true;

		softAngles();

		//float[] sombra=solarInformation.calculateStripShadow(solarInformation.getCalendar());
		float[] sombra=solarInformation.calculateRectangleShadow(solarInformation.getCalendar(),rquad);
		shadowsBuff=makeFloatBuffer(sombra);

		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
		gl.glMatrixMode(GL10.GL_MODELVIEW);		
		//gl.glColor4f(1.0f,1.0f,1.0f, 0.0f);				// Set The Color
		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glTranslatef(0,0,-5); // center scene

        // mundo
        gl.glPushMatrix();
			synchronized (this) {
				//Obtenemos la matriz de rotación simultanea en los 3 ejes
				Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), (float)(rquad*Math.PI/180.0)) );
				//La aplicamos al modelo multiplicandola con OpenGL
				gl.glMultMatrixf(qm, 0);
			}
			gl.glTranslatef(0f,0f, -0.01f); //un poco por debajo de la brújula
			drawWorld(gl);
		gl.glPopMatrix();
        
		// brújula
		gl.glPushMatrix(); 
			gl.glEnable(GL10.GL_TEXTURE_2D);						// Enable Texture Mapping 
						
			//Rotación de la brújula en los 3 ejes
			//Obtenemos la matriz de rotación simultanea en los 3 ejes
			Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), (float)(rquad*Math.PI/180.0)) );
			//La aplicamos al modelo multiplicandola con OpenGL
			gl.glMultMatrixf(qm, 0);
			
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

			//Rotación del poste en x e y
			//Obtenemos la matriz de rotación simultanea en los ejes x, y
			Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), 0) );
			//La aplicamos al modelo multiplicandola con OpenGL
			gl.glMultMatrixf(qm, 0);
			
			gl.glColor4f(0, 0, 1, 1.0f);
			gl.glTranslatef(0, 0, 0.5f);
			gl.glScalef(0.25f, 0.25f, 1.0f);
			drawCube(gl);
		gl.glPopMatrix();
	
		// sombra
		gl.glPushMatrix();
			synchronized (this) {
				//Obtenemos la matriz de rotación simultanea en los 3 ejes
//				Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), (float)(rquad*Math.PI/180.0)) );
				Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), 0 ) );
				//La aplicamos al modelo multiplicandola con OpenGL
				gl.glMultMatrixf(qm, 0);
			}
			gl.glTranslatef(0, 0, 0.01f);
			drawShadow(gl);
		gl.glPopMatrix();
		
    	float shadow = (float)solarInformation.getValue(SolarInformation.SHADOW_LENGTH_VALUE);
    	mMLabels.print(gl, mLabelShadow, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
        if ( shadow > 20.0f )
        	mMLabels.println(gl, mLabelNA, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
        else  
        	mMLabels.println(gl, (float)Math.floor(shadow*1000)/1000f, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	mMLabels.println(gl, (int)mLabelDate, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    	if ( location != null )
        	mMLabels.println(gl, mLabellocation, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );

    	if ( paused != 0 ) {
    		long delta = (System.currentTimeMillis() - paused_time);
    		long mod = delta % 1000;
    		if ( /*delta > 10000 ||*/ mod < 500 ) {
//            	mMLabels.println(gl, "", MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    			mMLabels.println(gl, mLabelRotation+paused-1, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );
    		}
    	}
    	if ( toggleDec > 0 )
    		mMLabels.println(gl, (long)toggleDec, MultiLabelMaker.HA_CENTER, MultiLabelMaker.VA_TOP );

        mMLabels.flush(gl, mWidth, mHeight);
        
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
	public int toggleDec = 0;
	
	float[][] rquad_window = new float[10][2];
	{ 
		rquad_window[0][0] = Float.NEGATIVE_INFINITY;
		rquad_window[0][1] =  (float)System.currentTimeMillis();
	}
	int window_size = rquad_window.length;
	int window_first = 0;
	int window_last = -1;
	
	
	private void softAngles() {
		synchronized (this) {
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

			float delta_log = delta;
			
			boolean discarded = true;
			if ( speed < 50f && delta != 0 ) {
				rquad_obj = (float)rquad_aux;
				discarded = false;
				if ( toggleDec == 1 )
					rquad_obj -= solarInformation.getValue(SolarInformation.DECLINATION_VALUE);
				if ( toggleDec == 2 )
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
//			Log.i( "ROTACIÓN", rquad_obj + " - " + speed + " - " + delta_raw + " - " + delta + " - " + rquad_log + " - " + rquad + " - " + inc );
//			Log.i( "ROTACIÓN", speed + " - " + rquad_obj + " - " + rquad_aux + " - " + delta_raw + " - " + (discarded?"DISCARDED":"")  );
//			Log.i("ROTACIÓN", 
//					 "s:"  + r(speed) + 
//					" o:" + r(rquad_obj) + 
//					" l:"  + r(rquad_window[window_last][0]) + 
//					" f:"  + r(rquad_window[window_first][0]) + 
//					" d:"  + r(delta_log) + 
//					" do:"  + r(delta*sign) + 
//					" - "   + (discarded ? "DISCARDED" : ""));

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
			//No se solarInformation sirve para algo pero me parecía interesante hacerlo ;-)
			xrot = softenDegrees(Math.abs(xrot))*Math.signum(xrot);
			yrot = softenDegrees(Math.abs(yrot))*Math.signum(yrot);

			if ( paused != 2 )
				xrot = (toggleX?xrot-10f:xrot-20f);
			
//			Log.i("", xrot + " - " + yrot  );
		}
	}
	
	static private String r( float v ) {
		
		char[] cad = "0000.000".toCharArray();
		String number = ""+v;
		int pos = number.indexOf('.');
		for ( int n = pos-1; n>=0; n-- )
			cad[4-pos+n] = number.charAt(n);
		for ( int n = pos+1; (4+n-pos) < cad.length && n < number.length(); n++ )
			cad[4+n-pos] = number.charAt(n);
		return new String(cad);
	}
}
