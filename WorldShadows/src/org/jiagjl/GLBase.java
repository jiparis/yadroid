package org.jiagjl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLU;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



public abstract class GLBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	protected EGLContext glContext;
	protected ViewAnimator animator;
	protected SurfaceHolder sHolder;
	protected Thread t;
	protected boolean running;
	int width;
	int height;
	boolean resize;
	int fps;
	
	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	protected static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}

	/**
	 * Make a direct NIO IntBuffer from an array of ints
	 * @param arr The array
	 * @return The newly created IntBuffer
	 */
	protected static IntBuffer makeFloatBuffer(int[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ib = bb.asIntBuffer();
		ib.put(arr);
		ib.position(0);
		return ib;
	}

	/**
	 * Create a texture and send it to the graphics system
	 * @param gl The GL object
	 * @param bmp The bitmap of the texture
	 * @param reverseRGB Should the RGB values be reversed?  (necessary workaround for loading .pngs...)
	 * @return The newly created identifier for the texture.
	 */
	protected static int loadTexture(GL10 gl, Bitmap bmp) {
		return loadTexture(gl, bmp, false);
	}
	
	/**
	 * Create a texture and send it to the graphics system
	 * @param gl The GL object
	 * @param bmp The bitmap of the texture
	 * @param reverseRGB Should the RGB values be reversed?  (necessary workaround for loading .pngs...)
	 * @return The newly created identifier for the texture.
	 */
	protected static int loadTexture(GL10 gl, Bitmap bmp, boolean reverseRGB) {
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		int[] ia = new int[h*w];

		bmp.getPixels(ia,0,w,0,0,w,h);
		for (int y=ia.length-1;y>=0;y--) {
			int pix = ia[y];
			// Convert ARGB -> RGBA
			@SuppressWarnings("unused")
			byte alpha = (byte)((pix >> 24)&0xFF);
			byte red = (byte)((pix >> 16)&0xFF);
			byte green = (byte)((pix >> 8)&0xFF);
			byte blue = (byte)((pix)&0xFF);
			
			// It seems like alpha is currently broken in Android...
			ia[y]= red << 24 | green << 16 | blue << 8 | 0xFF;//255-alpha);
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(h*w*4);
		bb.order(ByteOrder.BIG_ENDIAN);
		IntBuffer ib = bb.asIntBuffer();
		ib.put(ia);

		ib.position(0);
		bb.position(0);

		int[] tmp_tex = new int[1];

		gl.glGenTextures(1, tmp_tex, 0);
		int tex = tmp_tex[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bmp.getWidth(), bmp.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		return tex;
	}

	/**
	 * Constructor
	 * @param c The View's context.
	 */
	public GLBase(Context c) {
		this(c, -1);
	}

	/**
	 * Constructor for animated views
	 * @param c The View's context
	 * @param fps The frames per second for the animation.
	 */
	public GLBase(Context c, int fps) {
		super(c);
		sHolder = getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		this.fps = fps;
	}

	@Override
	protected void onAttachedToWindow() {
		if (animator != null) {
			// If we're animated, start the animation
			animator.start();
		}
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (animator != null) {
			// If we're animated, stop the animation
			animator.stop();
		}
		super.onDetachedFromWindow();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		synchronized (this) {
			this.width = width;
			this.height = height;
			this.resize = true;
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		t = new Thread(this);
		t.start();
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		running = false;
		try {
			t.join();
		}
		catch (InterruptedException ex) {}
		t = null;
	}

	public void run() {
		// Much of this code is from GLSurfaceView in the Google API Demos.
		// I encourage those interested to look there for documentation.
		EGL10 egl = (EGL10)EGLContext.getEGL();
		EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		
		int[] version = new int[2];
        egl.eglInitialize(dpy, version);
        
        int[] configSpec = {
                EGL10.EGL_RED_SIZE,      5,
                EGL10.EGL_GREEN_SIZE,    6,
                EGL10.EGL_BLUE_SIZE,     5,
                EGL10.EGL_DEPTH_SIZE,   16,
                EGL10.EGL_NONE
        };
        
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];
		
		EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, null);
		
		EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, sHolder, null);
		egl.eglMakeCurrent(dpy, surface, surface, context);
			
		GL10 gl = (GL10)context.getGL();

		gl = init(gl);
		
		int delta = -1;
		if (fps > 0) {
			delta = 1000/fps;
		}
		long time = System.currentTimeMillis();
		
		running = true;
		while (running) {
			int w, h;
			synchronized(this) {
				w = width;
				h = height;
			}
			if (System.currentTimeMillis()-time < delta) {
				try {
					Thread.sleep(System.currentTimeMillis()-time);
				}
				catch (InterruptedException ex) {}
			}
			drawFrame(gl, w, h);
			egl.eglSwapBuffers(dpy, surface);

            if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                Context c = getContext();
                if (c instanceof Activity) {
                    ((Activity)c).finish();
                }
            }
            time = System.currentTimeMillis();
		}
		
		end(gl);
		
        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
	}	

	private void drawFrame(GL10 gl, int w, int h) {
		if (resize) {
			resize(gl, w, h);
			resize = false;
		}
		drawFrame(gl);
	}
	
	protected void resize(GL10 gl, int w, int h) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glViewport(0,0,w,h);
		GLU.gluPerspective(gl, 45.0f, ((float)w)/h, 1f, 100f);
	}
	
	void GLDrawEllipse (GL10 gl, int segments, float width, float height, float centerx, float centery, boolean filled){
	
	 gl.glTranslatef(centerx, centery, 0.0f);
	 float vertices[] = new float[segments*2];
	 int count=0;
	 for (float i = 0; i < 360.0f; i+=(360.0f/segments))
	 {
	  vertices[count++] = (float) (Math.cos(degreesToRadian(i))*width);
	  vertices[count++] = (float) (Math.sin(degreesToRadian(i))*height);
	 }
	 gl.glVertexPointer (2, GL10.GL_FLOAT , 0, makeFloatBuffer(vertices)); 
	 gl.glDrawArrays ((filled) ? GL10.GL_TRIANGLE_FAN : GL10.GL_LINE_LOOP, 0, segments);
	}
	
	void GLDrawCircle (GL10 gl, int circleSegments, float circleSize, float centerx, float centery, boolean filled) 
	{
	 GLDrawEllipse(gl, circleSegments, circleSize, circleSize, centerx, centery, filled);
	}
	
	public void drawCube(GL10 gl){
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
	
	float degreesToRadian(float i){
		return (float) (i * Math.PI / 180f);
	}
	
	protected GL10 init(GL10 gl) {return gl;}

	protected void end(GL10 gl) {}
	
	protected abstract void drawFrame(GL10 gl);
}