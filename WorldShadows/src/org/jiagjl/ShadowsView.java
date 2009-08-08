package org.jiagjl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.jiagjl.drawtext.LabelMaker;
import org.jiagjl.drawtext.matrix.MatrixTrackingGL;

import org.jiagjl.drawtext.NumericSprite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;

public class ShadowsView extends GLBase {

	SolarInformation si;
	int time_window = 4*60; //En minutos
	
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
	
	float lightAmbient[]= { 0.5f, 0.5f, 0.5f, 1.0f }; 	// Ambient Light Values
	float lightDiffuse[]= { 1.0f, 1.0f, 1.0f, 1.0f };	// Diffuse Light Values 
	float lightPosition[]= { 0.0f, 0.0f, 2.0f, 1.0f };	// Light Position 	

	
	LabelMaker mLabels;
	NumericSprite mNumericSprite;
	Paint mLabelPaint;
	int mLabelNA;
	int mLabelNE, mLabelSE, mLabelNW, mLabelSW, mLabelDot;
	float[] qm = new float[16];
	
	static float DEGREES_MIN = 0.0f;
	static float DEGREES_MAX = 90.0f;
	static float FX_MIN = 0.0f;
	static float FX_MAX = 2.0f;
	static float DEGREES_TO_FX_FACTOR = Utils.scaleFactor( DEGREES_MIN, DEGREES_MAX, FX_MIN, FX_MAX ); 
	
	static float FX_RES_MIN = fx(FX_MIN);
	static float FX_RES_MAX = fx(FX_MAX);
	static float FX_RES_TO_DEGREES_FACTOR = Utils.scaleFactor( FX_RES_MIN, FX_RES_MAX, DEGREES_MIN, DEGREES_MAX ); 


	//Mapea los grados para que sigan una progresión exponencial en lugar de lineal
	static public float softenDegrees( float angle ) {
		return Utils.scale( fx( Utils.scale( angle, DEGREES_TO_FX_FACTOR, DEGREES_MIN, FX_MIN, FX_MAX )), FX_RES_TO_DEGREES_FACTOR, FX_RES_MIN, DEGREES_MIN, DEGREES_MAX );
	}

	static public float fx( float value ) {
		return (float)Math.exp(value);
	}

	boolean paused = false;
	boolean canPress = false;
	
	SensorListener sl = new SensorListener(){

		public void onAccuracyChanged(int sensor, int accuracy) {				
		}

		public void onSensorChanged(int sensor, float[] values) {
			synchronized (this) {
				if (!paused){
					//Azimuth - z				
					rquad = values[0];
					//Pitch - x
					xrot = values[1];
					//Roll - y
					yrot = values[2];	
	
					//Inclinamos la x un poco, porque es la forma natural del
					//teléfono en la mano
					xrot += 15.0f;
					if ( xrot > 90.0f )
						xrot = 90.0f;
					
					//Aplicamos una conversión a los grados de rotación x e y 
					//a través de una curva exponencial para que la inclinación
					//no sea lineal
					//No se si sirve para algo pero me parecía interesante hacerlo ;-)
					xrot = softenDegrees(Math.abs(xrot))*Math.signum(xrot);
					yrot = softenDegrees(Math.abs(yrot))*Math.signum(yrot);
				}
			}
	    }
	};
	
	public ShadowsView(Context c) {
		super(c, 5);
		quadBuff = makeFloatBuffer(quad);
		texBuff = makeFloatBuffer(texCoords);
		bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.compass);
		context = c;        

		linea1Buff = makeFloatBuffer(linea1);
		linea2Buff = makeFloatBuffer(linea2);
		linea3Buff = makeFloatBuffer(linea3);

		carreteraBuff = makeFloatBuffer(carretera);
		
		si = new SolarInformation();
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
	
		// Clear color (black)
		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.8f);
		
		// Load textures
		compassTex = loadTexture(gl, bmp);
		
		// lights
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);

		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
		
 
		//Los textos a pintar hay que crearlos primero usando un Paint, 
		//para que se carguen como texturas 2D a las que haremos 
		//referencia después
        mLabelPaint = new Paint();
        mLabelPaint.setTextSize(32);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);

        if (mLabels != null) {
            mLabels.shutdown(gl);
        } else {
            mLabels = new LabelMaker(true, 256, 256);
        }
        mLabels.initialize(gl);
        mLabels.beginAdding(gl);
        mLabelNA = mLabels.add(gl, "Fuera de rango", mLabelPaint);
        mLabelSW = mLabels.add(gl, "SW", mLabelPaint);
        mLabelNE = mLabels.add(gl, "NE", mLabelPaint);
        mLabelSE = mLabels.add(gl, "SE", mLabelPaint);
        mLabelNW = mLabels.add(gl, "NW", mLabelPaint);
        mLabelDot = mLabels.add(gl, ".", mLabelPaint);
        mLabels.endAdding(gl);
        
        if (mNumericSprite != null) {
            mNumericSprite.shutdown(gl);
        } else {
            mNumericSprite = new NumericSprite();
        }
        mNumericSprite.initialize(gl, mLabelPaint);

        return mgl;
	}

	
	@Override
	protected void drawFrame(GL10 gl) {				
		if (isPressed() && canPress){			
			paused = !paused;
			canPress = false;
		}		
		if (!isPressed())
			canPress = true;
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
		gl.glMatrixMode(GL10.GL_MODELVIEW);		
		gl.glColor4f(1.0f,1.0f,1.0f, 0.0f);				// Set The Color
		gl.glLoadIdentity();					// Reset The View, loading the identity matrix
		gl.glTranslatef(0,0,-5); // center scene

		//Pintamos los textos en la x,y de pantalla que queramos
		//El origen de coordenadas es la esquina inferior izquierda
		//La Z se utiliza para saber el orden de pintado. Si es >= 0
		//se pinta sobre la perspectiva que estamos haciendo
        float shadow = (float)si.getValue(SolarInformation.SHADOW_LENGTH_VALUE);

        if ( shadow > 20.0f ) {
			mLabels.beginDrawing(gl, mWidth, mHeight);
	        mLabels.draw(gl, (mWidth-mLabels.getWidth(mLabelNA))/2, mHeight-mLabels.getHeight(mLabelNA), 0, mLabelNA);
	        mLabels.endDrawing(gl);
        } else {
	        float width = mLabels.getWidth(mLabelDot);
	        
			int e = (int) Math.floor(shadow);
			int d = (int) Math.floor((shadow - e) * 1000);
	        mNumericSprite.setValue(e);
	        mNumericSprite.draw(gl, (mWidth-width)/2-mNumericSprite.width(), mHeight-mLabels.getHeight(mLabelNA), mWidth, mHeight);
			mLabels.beginDrawing(gl, mWidth, mHeight);
	        mLabels.draw(gl, (mWidth-width)/2, mHeight-mLabels.getHeight(mLabelNA), 0, mLabelDot);
	        mLabels.endDrawing(gl);
	        mNumericSprite.setValue(d);
	        mNumericSprite.draw(gl, (mWidth+width)/2, mHeight-mLabels.getHeight(mLabelNA), mWidth, mHeight);
        }

		// textured quad
		gl.glPushMatrix(); 
			gl.glEnable(GL10.GL_TEXTURE_2D);						// Enable Texture Mapping 
						
			//Rotación de la brújula en los 3 ejes
			synchronized (this) {
				//Obtenemos la matriz de rotación simultanea en los 3 ejes
				Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(-xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), (float)(rquad*Math.PI/180.0)) );
				//La aplicamos al modelo multiplicandola con OpenGL
				gl.glMultMatrixf(qm, 0);
			}

			// textura a aplicar
			gl.glBindTexture(GL10.GL_TEXTURE_2D, compassTex);
		
			// send vertices to the renderer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, quadBuff);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				
			// send texture coords to the renderer
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuff);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
				
			// draw!
			gl.glNormal3f(0,0,1.0f);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);		

			//Pinta etiquetas de dirección en cada uno de los vértices 
			//de la brújula. Si el objeto gl no es del tipo MatrixTrackingGL,
			//no será capaz de calcular la proyección de cada vértice,
			//por lo que pintaría los textos en la coordenada 0,0
			mLabels.beginDrawing(gl, mWidth, mHeight);
	        mLabels.draw(gl, quad[0], quad[1], quad[2], mLabelSW, 0);
	        mLabels.draw(gl, quad[3], quad[4], quad[5], mLabelSE, 0);
	        mLabels.draw(gl, quad[6], quad[7], quad[8], mLabelNW, 0);
	        mLabels.draw(gl, quad[9], quad[10], quad[11], mLabelNE, 0);
	        mLabels.endDrawing(gl);
		gl.glPopMatrix();
		
		// prueba de circle
//		gl.glPushMatrix();
//			gl.glColor4f(0.0f,0.0f,0.0f, 0.0f);				// Set The Color
//			GLDrawCircle(gl, 10, 0.5f, 0, -2, true);
//		gl.glPopMatrix();
		
		// poste
		gl.glPushMatrix();
			gl.glLoadIdentity();					// Reset The View, loading the identity matrix
			gl.glTranslatef(0,0,-5); // center scene

			//Rotación del poste en x e y
			synchronized (this) {
				//Obtenemos la matriz de rotación simultanea en los ejes x, y
				Utils.quatToMatrix( qm, 0, Utils.eulerToQuat((float)(-xrot*Math.PI/180.0), (float)(yrot*Math.PI/180.0), 0) );
				//La aplicamos al modelo multiplicandola con OpenGL
				gl.glMultMatrixf(qm, 0);
			}
			
			gl.glColor4f(0, 0, 1, 1.0f);
			gl.glTranslatef(0, 0, 0.5f);
			gl.glScalef(0.25f, 0.25f, 1.0f);
			drawPoste(gl);
		gl.glPopMatrix();

	}

	int mWidth;
	int mHeight;
	
	protected void resize(GL10 gl, int w, int h) {
		super.resize(gl, w, h );
		mWidth = w;
		mHeight= h;
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

		gl.glNormal3f(0,0,1);
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
