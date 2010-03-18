// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   gmModelAreas.java

package org.andamobile.ashadow.geomag;


public class gmModelAreas 
{

	int mId;
	
    private gmModelAreas(int id)
    {
        mId = id;
    }

    public static final int intINVALID = 0;
    public static final int intGLOBAL = 1;
    public static final int intREGION = 2;
    public static final gmModelAreas INVALID = new gmModelAreas(0);
    public static final gmModelAreas GLOBAL = new gmModelAreas(1);
    public static final gmModelAreas REGION = new gmModelAreas(2);

}
