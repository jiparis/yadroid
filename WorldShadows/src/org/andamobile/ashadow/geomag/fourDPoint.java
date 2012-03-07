// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   fourDPoint.java

package org.andamobile.ashadow.geomag;


// Referenced classes of package gov.usgs.apps.magcalc:
//            geographAxes

public class fourDPoint
    implements geographAxes
{

    public fourDPoint()
    {
        geoPoint = new double[4];
        geoPoint[0] = 0.0D;
        geoPoint[1] = 0.0D;
        geoPoint[2] = 0.0D;
        geoPoint[3] = 0.0D;
    }

    public fourDPoint(double lat, double lon)
    {
        geoPoint = new double[4];
        geoPoint[0] = lat;
        geoPoint[1] = lon;
        geoPoint[2] = 0.0D;
        geoPoint[3] = 0.0D;
    }

    public fourDPoint(double lat, double lon, double ele, double tm)
    {
        geoPoint = new double[4];
        setPoint(lat, lon, ele, tm);
    }

    public void setPoint(double lat, double lon, double ele, double tm)
    {
        geoPoint[0] = lat;
        geoPoint[1] = lon;
        geoPoint[2] = ele;
        geoPoint[3] = tm;
    }

    public void setLatitude(double lat)
    {
        geoPoint[0] = lat;
    }

    public void setLongitude(double lon)
    {
        geoPoint[1] = lon;
    }

    public void setElevation(double ele)
    {
        geoPoint[2] = ele;
    }

    public void setTime(double tm)
    {
        geoPoint[3] = tm;
    }

    public double[] getPoint()
    {
        double dbl[] = new double[4];
        dbl[0] = geoPoint[0];
        dbl[1] = geoPoint[1];
        dbl[2] = geoPoint[2];
        dbl[3] = geoPoint[3];
        return dbl;
    }

    public double getLatitude()
    {
        return geoPoint[0];
    }

    public double getLongitude()
    {
        return geoPoint[1];
    }

    public double getElevation()
    {
        return geoPoint[2];
    }

    public double getTime()
    {
        return geoPoint[3];
    }

    private double geoPoint[];
}
