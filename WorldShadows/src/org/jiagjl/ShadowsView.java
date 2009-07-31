package org.jiagjl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class ShadowsView extends GLTutorialBase {

	float[] quad = new float[]{
			-1.0f,-1.0f, 0.0f,
			 1.0f,-1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f		
	};
	
	float[] colors = new float[]{
			1, 0,0,1,
			0,1,0,1,
			0,0,1,1,
			1,1,1,0.5f
	};
	
	float texCoords[] = new float[] {			
			0.0f, 1.0f,		// bottom-left	
			1.0f, 1.f,		// bottom-right
			0.0f, 0.0f,		// top-left
			1.0f, 0.0f		// top-right	 
	};
	
	FloatBuffer quadBuff;
	FloatBuffer colBuff;
	FloatBuffer texBuff;

	
	float[] linea1 = new float[] { 0.0f, 0.0f, 0.01f, 1.3f, 0.0f, 0.01f, };
	float[] linea2 = new float[] { 0.0f, 0.0f, 0.01f, 0.0f, 1.3f, 0.01f, };
	float[] linea3 = new float[] { 0.0f, 0.0f, 0.01f, 0.0f, 0.0f, 1.3f, };
	
	FloatBuffer linea1Buff;
	FloatBuffer linea2Buff;
	FloatBuffer linea3Buff;

	float[] carretera = new float[] { -1.4f, -10.0f, -0.0f, -1.4f, 10.0f, 0.0f,
									   1.4f, -10.0f, -0.0f,  1.4f, 10.0f, 0.0f,};

	FloatBuffer carreteraBuff;
	
	float rquad;
	float xrot = 0.0f;
	float yrot = 0.0f;
	
	int compassTex;
	
	Bitmap bmp;
	
	Context context;
	SensorListener sl = new SensorListener(){

		public void onAccuracyChanged(int sensor, int accuracy) {				
		}

		public void onSensorChanged(int sensor, float[] values) {
				//Azimuth - z
				rquad = values[0];
				//Pitch - x
				xrot = values[1];
				//Roll - y
				yrot = values[2];			
		}
    	
    };
	
	public ShadowsView(Context c) {
		super(c, 5);
		quadBuff = makeFloatBuffer(quad);
		colBuff = makeFloatBuffer(colors);
		texBuff = makeFloatBuffer(texCoords);
		bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.compass);
		context = c;
		
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sl, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_GAME);

		linea1Buff = makeFloatBuffer(linea1);
		linea2Buff = makeFloatBuffer(linea2);
		linea3Buff = makeFloatBuffer(linea3);

		carreteraBuff = makeFloatBuffer(carretera);
	}

	@Override
	protected void end(GL10 gl) {
		super.end(gl);
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(sl);
	}

	@Override
	protected void init(GL10 gl){		
		// Smooth shading
		gl.glShadeModel(GL10.GL_SMOOTH);	// Enables Smooth shading
		
		// Depth buffer
		gl.glClearDepthf(1.0f);				// Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST);	// Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL);		// The Type Of Depth Test To Do
		
		// perspective
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);		// Really Nice Perspective Calculations
	
		// Clear color (black)
		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.8f);
		
		// Load textures
		compassTex = loadTexture(gl, bmp);
	}
	
	@Override
	protected void drawFrame(GL10 gl) {		
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
		gl.glMatrixMode(GL10.GL_MODELVIEW);		

		gl.glColor4f(1.0f,1.0f,1.0f, 0.0f);				// Set The Color
		
		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glTranslatef(0,0,-5); // center scene
		gl.glRotatef(xrot, 1.0f, 0, 0);
		gl.glRotatef(rquad,0.0f,0.0f,1.0f);	// rotate scene on z axis

		// textured quad
		gl.glPushMatrix(); 
			gl.glEnable(GL10.GL_TEXTURE_2D);						// Enable Texture Mapping 
						
			// textura a aplicar
			gl.glBindTexture(GL10.GL_TEXTURE_2D, compassTex);
		
			// send vertices to the renderer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, quadBuff);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				
			// send texture coords to the renderer
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuff);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
				
			// draw!
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);		
		gl.glPopMatrix();
		
		// prueba de circle
//		gl.glPushMatrix();
//			gl.glColor4f(0.0f,0.0f,0.0f, 0.0f);				// Set The Color
//			GLDrawCircle(gl, 10, 0.5f, 0, -2, true);
//		gl.glPopMatrix();
		
		// poste
		gl.glPushMatrix();
			gl.glColor4f(0, 0, 1, 0.9f);
			gl.glTranslatef(0, 0, 0.5f);
			gl.glScalef(0.25f, 0.25f, 1.0f);
			drawPoste(gl);
		gl.glPopMatrix();		
	}
	
	public void drawPoste(GL10 gl){
		float box[] = new float[] {
				// FRONT
				-0.5f, -0.5f,  0.5f,
				 0.5f, -0.5f,  0.5f,
				-0.5f,  0.5f,  0.5f,
				 0.5f,  0.5f,  0.5f,
				// BACK
				-0.5f, -0.5f, -0.5f,
				-0.5f,  0.5f, -0.5f,
				 0.5f, -0.5f, -0.5f,
				 0.5f,  0.5f, -0.5f,
				// LEFT
				-0.5f, -0.5f,  0.5f,
				-0.5f,  0.5f,  0.5f,
				-0.5f, -0.5f, -0.5f,
				-0.5f,  0.5f, -0.5f,
				// RIGHT
				 0.5f, -0.5f, -0.5f,
				 0.5f,  0.5f, -0.5f,
				 0.5f, -0.5f,  0.5f,
				 0.5f,  0.5f,  0.5f,
				// TOP
				-0.5f,  0.5f,  0.5f,
				 0.5f,  0.5f,  0.5f,
				 -0.5f,  0.5f, -0.5f,
				 0.5f,  0.5f, -0.5f,
				// BOTTOM
				-0.5f, -0.5f,  0.5f,
				-0.5f, -0.5f, -0.5f,
				 0.5f, -0.5f,  0.5f,
				 0.5f, -0.5f, -0.5f,
			};
		
		FloatBuffer cubeBuff = makeFloatBuffer(box);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeBuff);

		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		gl.glNormal3f(0,0,-1);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
	
		gl.glNormal3f(-1,0,0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
		gl.glNormal3f(1,0,0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);
		
		gl.glNormal3f(0,1,0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
		gl.glNormal3f(0,-1,0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
	}

}
