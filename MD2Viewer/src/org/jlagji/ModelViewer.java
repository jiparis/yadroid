package org.jlagji;

import java.io.InputStream;

import org.jlagji.loader.IntMesh;
import org.jlagji.loader.MD2Loader;
import org.jlagji.loader.Model;

import android.app.Activity;
import android.os.Bundle;


public class ModelViewer extends Activity {
	InputStream is;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		is = getResources().openRawResource(R.drawable.tris);
		
		MD2Loader ld = new MD2Loader();
		ld.setFactory(IntMesh.factory());
		
		try {
			Model model = ld.load(is, 0.1f, "skin.jpg");
			if (model.getFrameCount() > 1)
				setContentView(new ModelViewInterpolated(model, this));
			else
				setContentView(new ModelView(model, this));
		} 
		catch (java.io.IOException ex) {
			setContentView(R.layout.main);
		}
	}
}