package org.jlagji;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.jlagji.loader.FixedPointUtils;
import org.jlagji.loader.Mesh;
import org.jlagji.loader.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLU;
import android.view.KeyEvent;
import android.view.View;


public class ModelView extends GLBase {
	private org.jlagji.loader.Model m;
	ViewAnimator animator;

	int tex;
	int verts;

	float lightAmbient[] = new float[] { 0.2f, 0.3f, 0.6f, 1.0f };
	float lightDiffuse[] = new float[] { 1f, 1f, 1f, 1.0f };

	float matAmbient[] = new float[] { 1f, 1f, 1f, 1.0f };
	float matDiffuse[] = new float[] { 1f, 1f, 1f, 1.0f };

	float[] pos = new float[] {0,20,20,1};

	int frame_ix = 0;

	private IntBuffer[] vertices;
	private IntBuffer normals;
	private IntBuffer texCoords;
	private ShortBuffer indices;

//	protected static int loadTexture(GL10 gl, Bitmap bmp) {
//		ByteBuffer bb = ByteBuffer.allocateDirect(bmp.getHeight()*bmp.getWidth()*4);
//		bb.order(ByteOrder.nativeOrder());
//		IntBuffer ib = bb.asIntBuffer();
//
//		for (int y=0;y<bmp.getHeight();y++)
//			for (int x=0;x<bmp.getWidth();x++) {
//				ib.put(bmp.getPixel(x,y));
//			}
//		ib.position(0);
//		bb.position(0);
//
//		int[] tmp_tex = new int[1];
//
//		gl.glGenTextures(1, tmp_tex, 0);
//		int tx = tmp_tex[0];
//		gl.glBindTexture(GL10.GL_TEXTURE_2D, tx);
//		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bmp.getWidth(), bmp.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//		return tx;
//	}

	public ModelView(Model m, Context c) {
		super(c, 5);
		setFocusable(true);
		this.m = m;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
			frame_ix--;
			if (frame_ix < 0)
				frame_ix = m.getFrameCount()-1;
			invalidate();
		}
		else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
			frame_ix = (frame_ix+1)%m.getFrameCount();
			invalidate();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void init(GL10 gl){
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(1,1,1,1);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_NORMALIZE);


		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, pos, 0);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);

		// Pretty perspective
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_CULL_FACE);
		// Turn on these for textures.

		ByteBuffer bb;
		vertices = new IntBuffer[m.getFrameCount()];
		for (int i=0;i<m.getFrameCount();i++) {
			Mesh ms = m.getFrame(i).getMesh();
			verts = ms.getFaceCount()*3;

			//bb = ByteBuffer.allocateDirect(msh.getVertexCount()*3*4);
			bb = ByteBuffer.allocateDirect(verts*3*4);
			bb.order(ByteOrder.nativeOrder());
			vertices[i] = bb.asIntBuffer();

			for (int f=0;f<ms.getFaceCount();f++) {
				int[] face = ms.getFace(f);
				for (int j=0;j<3;j++) {
					float[] v = ms.getVertexf(face[j]);
					for (int k=0;k<3;k++) {
						vertices[i].put(FixedPointUtils.toFixed(v[k]));
					}
				}
			}
			vertices[i].position(0);
		}

		Mesh msh = m.getFrame(0).getMesh();
		if (msh.getTextureFile() != null) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
			tex = loadTexture(gl, BitmapFactory.decodeResource(getContext().getResources(),R.drawable.skin));
		}

		//bb = ByteBuffer.allocateDirect(msh.getVertexCount()*3*4);
		bb = ByteBuffer.allocateDirect(verts*3*4);
		bb.order(ByteOrder.nativeOrder());
		normals = bb.asIntBuffer();

		bb = ByteBuffer.allocateDirect(verts*2*4);
		bb.order(ByteOrder.nativeOrder());
		texCoords = bb.asIntBuffer();

		bb = ByteBuffer.allocateDirect(verts*2);
		bb.order(ByteOrder.nativeOrder());
		indices = bb.asShortBuffer();

		short ct = 0;
		for (int i=0;i<msh.getFaceCount();i++) {
			int[] face_n = msh.getFaceNormals(i);
			int[] face_tx = msh.getFaceTextures(i);
			for (int j=0;j<3;j++) {
				float[] n = msh.getNormalf(face_n[j]);
				for (int k=0;k<3;k++) {
					normals.put(FixedPointUtils.toFixed(n[k]));
				}
				float[] tx = msh.getTextureCoordinatef(face_tx[j]);
				texCoords.put(FixedPointUtils.toFixed(tx[0]));
				texCoords.put(FixedPointUtils.toFixed(tx[1]));				
				indices.put(ct++);
			}
		}
		normals.position(0);
		texCoords.position(0);
		indices.position(0);
	}

	@Override
	protected void drawFrame(GL10 gl) {
		int w = getWidth();
		int h = getHeight();

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glViewport(0,0,w,h);
		GLU.gluPerspective(gl, 45.0f, ((float)w)/h, 1f, 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0);
		gl.glTranslatef(0,0,-10);
		gl.glRotatef(30.0f, 1, 0, 0);
		//gl.glRotatef(40.0f, 0, 1, 0);

		gl.glVertexPointer(3,GL10.GL_FIXED, 0, vertices[frame_ix]);
		gl.glNormalPointer(GL10.GL_FIXED,0, normals);
		gl.glTexCoordPointer(2,GL10.GL_FIXED,0,texCoords);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		//gl.glColor4f(1,0,0,1);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		//gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		gl.glDrawElements(GL10.GL_TRIANGLES, verts, GL10.GL_UNSIGNED_SHORT, indices);

		frame_ix = (frame_ix+1)%m.getFrameCount();
	}
}
