package org.jiagjl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SolarInformation {

	final static public int LOCAL_VALUE = 0;
	final static public int SOLAR_VALUE = 1;
	final static public int ALTITUDE_VALUE = 2;
	final static public int AZIMUT_VALUE = 3;
	final static public int SUNRISE_VALUE = 4;
	final static public int SUNSET_VALUE = 5;
	final static public int SHADOW_LENGTH_VALUE = 6;
	final static public int TIME_WINDOW_VALUE = 7;

	final static public int LOCAL_TIME = 100;
	final static public int SOLAR_TIME = 101;
	final static public int SUNRISE_TIME = 102;
	final static public int SUNSET_TIME = 103;

	
	Calendar calendar = new GregorianCalendar();//Calendar.getInstance();
	float time_zone   = calendar.get(Calendar.ZONE_OFFSET) / 3600000.0f; // 1000 * 60 * 60
	double latitude   = 37.36d;
	double longitude  = -5.97d;
	int time_window = 4*60; //En minutos

	boolean recalculate = true;
	
	
	synchronized public void setTimeZone( float timeZone ) {
		time_zone = timeZone;
		recalculate = true;
	}

	synchronized public void setTimeWindow( int timeWindow ) {
		time_window = timeWindow;
	}
	
	synchronized public void setEndTime( int hour, int minute ) {
		time_window = (hour*60+minute)-(calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE));;
		if ( time_window < 0 )
			time_window = 4*60;
	}
	
	synchronized public void setPosition( double latitude, double longitude ) {
		this.latitude = latitude;
		this.longitude = longitude;
		recalculate = true;
	}
	
	synchronized public void setDate( int year, int month, int day ) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 18);
		recalculate = true;
	}
	
	synchronized public void setTime( int hour, int minute ) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		recalculate = true;
	}
	
	
	// Solar information.
	double fLocalTime;
	double fSolarTime;
	// Solar altitude is the angle up from the horizon. Zero degrees altitude
	// means exactly on your local horizon, and 90 degrees is "straight up".
	// Hence, "directly underfoot" is -90 degrees altitude.
	double fAltitude;
	// Azimuth is the angle along the horizon, with zero degrees corresponding
	// to North, and increasing in a clockwise fashion. Thus, 90 degrees is
	// East, 180 degrees is South, and 270 degrees is West. Using these two
	// angles, one can describe the apparent position of an object (such as the
	// Sun at a given time). The azimuth values are computed with respect to
	// true north (not magnetic).
	double fAzimuth;
	double fSunrise;
	double fSunset;
	// Relative shadow length for a vertical element of height h
	double fShadowLength;

	synchronized public double getValue(int field) {
		if ( recalculate )
			compute(latitude, longitude, time_zone, calendar );

		double res = 0;
		switch (field) {
		case LOCAL_VALUE:
			res = fLocalTime;
			break;
		case SOLAR_VALUE:
			res = fSolarTime;
			break;
		case ALTITUDE_VALUE:
			res = fAltitude;
			break;
		case AZIMUT_VALUE:
			res = fAzimuth;
			break;
		case SUNRISE_VALUE:
			res = fSunrise;
			break;
		case SUNSET_VALUE:
			res = fSunset;
			break;
		case SHADOW_LENGTH_VALUE:
			res = fShadowLength;
			break;
		case TIME_WINDOW_VALUE:
			res = (float)time_window;
			break;
		default:
			break;
		}
		return res;
	}

	synchronized public String getTime(int field) {
		if ( recalculate )
			compute(latitude, longitude, time_zone, calendar );
		
		double res = 0;
		switch (field) {
		case LOCAL_TIME:
			res = fLocalTime;
			break;
		case SOLAR_TIME:
			res = fSolarTime;
			break;
		case SUNRISE_TIME:
			res = fSunrise;
			break;
		case SUNSET_TIME:
			res = fSunset;
			break;
		default:
			break;
		}
		String minute, hour;
		int t = (int) Math.floor(res);
		int m = (int) Math.floor((res - t) * 60.0);
		if (m < 10) minute = "0" + m; else minute = ""+m;
		if (t < 10) hour = "0" + t; else hour = ""+t;
		
		return hour + ":" + minute;
	}
	
	synchronized public Calendar getCalendar() {
		return calendar;
	}

//	private String parse_time(String inputString) {
//
//		if (inputString.charAt(1) == ':') {
//			return (inputString.substring(0, 1) + inputString.substring(2,
//					inputString.length()));
//		}
//
//		if (inputString.charAt(2) == ':') {
//			return (inputString.substring(0, 2) + inputString.substring(3,
//					inputString.length()));
//		}
//
//		return (inputString);
//
//	}
//
//	static int[] month_day_convert = new int[] { 0, 31, 59, 90, 120, 151, 181,
//			212, 243, 273, 304, 334 };


	synchronized public float[] calculateShadowsRange( float[] shadows, int step ) {
		int size = (int)Math.round( time_window / step + 0.5);
		if ( shadows == null || (shadows.length / 2) != size )
			shadows = new float[size*2];
		calendar.add(Calendar.MINUTE, size*step);
		for ( int n = size-1; n >= 0; n-- ) {
			calendar.add( Calendar.MINUTE , -1*step);
			compute( latitude, longitude, time_zone, calendar );
			shadows[n*2] = (float)getValue(SolarInformation.AZIMUT_VALUE);
			shadows[n*2+1] = (float)getValue(SolarInformation.SHADOW_LENGTH_VALUE);
		}
		return shadows;
	}

	private void compute(double latitude, double longitude, float timeZone, Calendar instant) {
		// Location in radians.
		double fLatitude;
		double fLongitude;
		double fTimeZone;

		// Calculated data.
		double fDifference;
		double fDeclination;
		double fEquation; // Equation of time (minutes)

		// Integer values.
		int iJulianDate;

		// Temp data.
		double t, hour, minute, test;

		// //////////////////////////////////////////////////////////
		// READ INPUT DATA
		// //////////////////////////////////////////////////////////

		// Get location data.
		fLatitude = latitude * (Math.PI / 180.0);
		fTimeZone = timeZone * 15 * (Math.PI / 180.0);
		fLongitude = longitude * (Math.PI / 180.0);
		// Get julian date.
		// iJulianDate = month_day_convert[instant.get(Calendar.MONTH)] +
		// instant.get(Calendar.DAY_OF_MONTH);
		iJulianDate = instant.get(Calendar.DAY_OF_YEAR);

		// Get local time value.
//		fLocalTime = Float.parseFloat(parse_time("18:00"));
        //fLocalTime = Float.parseFloat(DateFormat.format("hhmm", instant).toString());
        fLocalTime = Float.parseFloat(new SimpleDateFormat("HHmm").format(instant.getTime()));

		// Check for military time (2400).
		if (fLocalTime > 100) {
			fLocalTime /= 100.0;
			hour = Math.floor(fLocalTime);
			minute = Math.round((fLocalTime - hour) * 100.0);
			fLocalTime = hour + (minute / 60f);
		}

		// //////////////////////////////////////////////////////////
		// CALCULATE SOLAR VALUES
		// //////////////////////////////////////////////////////////

		// Preliminary check.
		if (iJulianDate > 365)
			iJulianDate -= 365;
		if (iJulianDate < 0)
			iJulianDate += 365;

		// Secondary check of julian date.
		if (iJulianDate > 365)
			iJulianDate = 365;
		if (iJulianDate < 1)
			iJulianDate = 1;

		// Calculate solar declination as per Carruthers et al.
		t = 2 * Math.PI * ((iJulianDate - 1) / 365f);

		fDeclination = (0.322003 - 22.971 * Math.cos(t) - 0.357898
				* Math.cos(2 * t) - 0.14398 * Math.cos(3 * t) + 3.94638
				* Math.sin(t) + 0.019334 * Math.sin(2 * t) + 0.05928 * Math
				.sin(3 * t));

		// Convert degrees to radians.
		if (fDeclination > 89.9)
			fDeclination = 89.9;
		if (fDeclination < -89.9)
			fDeclination = -89.9;

		// Convert to radians.
		fDeclination = fDeclination * (Math.PI / 180.0);

		// Calculate the equation of time as per Carruthers et al.
		t = (279.134 + 0.985647 * iJulianDate) * (Math.PI / 180.0);

		fEquation = (5.0323 - 100.976 * Math.sin(t) + 595.275 * Math.sin(2 * t)
				+ 3.6858 * Math.sin(3 * t) - 12.47 * Math.sin(4 * t) - 430.847
				* Math.cos(t) + 12.5024 * Math.cos(2 * t) + 18.25 * Math
				.cos(3 * t));

		// Convert seconds to hours.
		fEquation = fEquation / 3600.00;

		// Calculate difference (in minutes) from reference longitude.
		fDifference = (((fLongitude - fTimeZone) * 180 / Math.PI) * 4) / 60.0;

		// Convert solar noon to local noon.
		double local_noon = 12.0 - fEquation - fDifference;

		// Calculate angle normal to meridian plane.
		if (fLatitude > (0.99 * (Math.PI / 2.0)))
			fLatitude = (0.99 * (Math.PI / 2.0));
		if (fLatitude < -(0.99 * (Math.PI / 2.0)))
			fLatitude = -(0.99 * (Math.PI / 2.0));

		test = -Math.tan(fLatitude) * Math.tan(fDeclination);

		if (test < -1)
			t = Math.acos(-1.0) / (15 * (Math.PI / 180.0));
		// else if (test > 1) t = acos(1.0) / (15 * (Math.PI / 180.0)); ###
		// Correction - missing 'Math.acos'
		else if (test > 1)
			t = Math.acos(1.0) / (15 * (Math.PI / 180.0));
		else
			t = Math.acos(-Math.tan(fLatitude) * Math.tan(fDeclination))
					/ (15 * (Math.PI / 180.0));

		// Sunrise and sunset.
		fSunrise = local_noon - t;
		fSunset = local_noon + t;

		// Check validity of local time.
		if (fLocalTime > fSunset)
			fLocalTime = fSunset;
		if (fLocalTime < fSunrise)
			fLocalTime = fSunrise;
		if (fLocalTime > 24.0)
			fLocalTime = 24.0;
		if (fLocalTime < 0.0)
			fLocalTime = 0.0;

		// Caculate solar time.
		fSolarTime = fLocalTime + fEquation + fDifference;

		// Calculate hour angle.
		double fHourAngle = (15 * (fSolarTime - 12)) * (Math.PI / 180.0);

		// Calculate current altitude.
		t = (Math.sin(fDeclination) * Math.sin(fLatitude))
				+ (Math.cos(fDeclination) * Math.cos(fLatitude) * Math
						.cos(fHourAngle));
		fAltitude = Math.asin(t);

		// Original calculation of current azimuth - LEAVE COMMENTED AS DOES NOT
		// WORK CORRECTLY.
		// t = (Math.cos(fLatitude) * Math.sin(fDeclination)) -
		// (Math.cos(fDeclination) * Math.sin(fLatitude) *
		// Math.cos(fHourAngle));
		// fAzimuth = Math.acos(t / Math.cos(fAltitude));

		// FIX:
		// ##########################################
		// Need to do discrete quadrant checking.

		// Calculate current azimuth.
		t = (Math.sin(fDeclination) * Math.cos(fLatitude))
				- (Math.cos(fDeclination) * Math.sin(fLatitude) * Math
						.cos(fHourAngle));

		double sin1;
		double cos2;
		// Avoid division by zero error.
		if (fAltitude < (Math.PI / 2.0)) {
			sin1 = (-Math.cos(fDeclination) * Math.sin(fHourAngle))
					/ Math.cos(fAltitude);
			cos2 = t / Math.cos(fAltitude);
		}

		else {
			sin1 = 0.0;
			cos2 = 0.0;
		}

		// Some range checking.
		if (sin1 > 1.0)
			sin1 = 1.0;
		if (sin1 < -1.0)
			sin1 = -1.0;
		if (cos2 < -1.0)
			cos2 = -1.0;
		if (cos2 > 1.0)
			cos2 = 1.0;

		// Calculate azimuth subject to quadrant.
		if (sin1 < -0.99999)
			fAzimuth = Math.asin(sin1);

		else if ((sin1 > 0.0) && (cos2 < 0.0)) {
			if (sin1 >= 1.0)
				fAzimuth = -(Math.PI / 2.0);
			else
				fAzimuth = (Math.PI / 2.0)
						+ ((Math.PI / 2.0) - Math.asin(sin1));
		}

		else if ((sin1 < 0.0) && (cos2 < 0.0)) {
			if (sin1 <= -1.0)
				fAzimuth = (Math.PI / 2.0);
			else
				fAzimuth = -(Math.PI / 2.0)
						- ((Math.PI / 2.0) + Math.asin(sin1));
		}

		else
			fAzimuth = Math.asin(sin1);

		// A little last-ditch range check.
		if ((fAzimuth < 0.0) && (fLocalTime < 10.0)) {
			fAzimuth = -fAzimuth;
		}

//		t = 10.0 / Math.tan(fAltitude);
//		System.out.println( "Shadow 1: " + Math.round(t / 10.0 ) / 1000.0 );
		
		//From radias to degree
		fDeclination = Math.round(fDeclination * (180.0/Math.PI) * 100) / 100.0;
		fAltitude = Math.round(fAltitude * (180.0/Math.PI) * 100) / 100.0;
		fAzimuth = Math.round(fAzimuth * (180.0/Math.PI) * 100) / 100.0;

		t = Math.tan((90 - fAltitude) / 57.295779513082320876798154814105);
		fShadowLength = Math.round(t * 100) / 100.0;


		recalculate = false;
	}
	

	static public void main( String[] argv ) {
		SolarInformation solin = new SolarInformation();
		solin.setDate(2009, 7, 8);
		solin.setTime(19, 32);
		solin.setTimeZone(2.0f);
		solin.setPosition(37.36d, -5.97d);
		System.out.println( "Altitude: " + solin.getValue( ALTITUDE_VALUE ) );
		System.out.println( "Azimut:   " + solin.getValue( AZIMUT_VALUE ) );
		System.out.println( "Shadow:   " + solin.getValue( SHADOW_LENGTH_VALUE ) );
		System.out.println( "Local:    " + solin.getTime( LOCAL_TIME ) );
		System.out.println( "Solar:    " + solin.getTime( SOLAR_TIME ) );
		System.out.println( "Sunrise:  " + solin.getTime( SUNRISE_TIME ) );
		System.out.println( "Sunset:   " + solin.getTime( SUNSET_TIME ) );
	}
	
}
