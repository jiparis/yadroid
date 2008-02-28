package com.thegame.engine;

import com.google.android.maps.Point;

public class CollisionUtils {
	public static boolean rectangleHit(GameObject spr1, GameObject spr2) {		
		Point p1 = spr1.getXY();
		Point p2 = spr2.getXY();
		
		int x1 = p1.getLongitudeE6();
		int x2 = p2.getLongitudeE6();
		int y1 = p1.getLatitudeE6();
		int y2 = p2.getLatitudeE6();
		if (x1<0) x1 = -x1;
		if (x2<0) x2 = -x2;
		if (y1<0) y1 = -y1;
		if (y2<0) y2 = -y2;

		if (-x1 + x2 + spr2.getWidth() - 1 >= 0
				&& x1 + spr1.getWidth() - 1 - x2 >= 0) {
			if (-y1 + y2 + spr2.getHeight() - 1 >= 0
					&& y1 + spr1.getHeight() - 1 - y2 >= 0)
				return true; // collision!
		}
		return false; // no collision!
	}

	public static boolean circleHit(GameObject spr1, GameObject spr2) {
		Point p1 = spr1.getXY();
		Point p2 = spr2.getXY();
		// raggio oggetto 1
		double r1 = (spr1.getHeight() * spr1.getHeight() + spr1.getWidth()
				* spr1.getWidth()) / 2;
		// raggio oggetto 2
		double r2 = (spr2.getHeight() * spr2.getHeight() + spr2.getWidth()
				* spr2.getWidth()) / 2;
		// distanza centri
		double d = Math.pow((p1.getLongitudeE6() - p2.getLongitudeE6()), 2)
				+ Math.pow((p1.getLatitudeE6() - p2.getLatitudeE6()), 2);
		// test
		if (d < r1 + r2)
			return true;
		return false;
	}


}
