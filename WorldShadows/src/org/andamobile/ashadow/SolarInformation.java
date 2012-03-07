package org.andamobile.ashadow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.andamobile.ashadow.geomag.geomagModel;



public class SolarInformation {

	final static public int LOCAL_VALUE = 0;
	final static public int SOLAR_VALUE = 1;
	final static public int ALTITUDE_VALUE = 2;
	final static public int AZIMUT_VALUE = 3;
	final static public int SUNRISE_VALUE = 4;
	final static public int SUNSET_VALUE = 5;
	final static public int SHADOW_LENGTH_VALUE = 6;
	final static public int TIME_WINDOW_VALUE = 7;
	final static public int DECLINATION_VALUE = 8;
	final static public int LATITUDE_VALUE = 9;
	final static public int LONGITUDE_VALUE = 10;

	final static public int LOCAL_TIME = 100;
	final static public int SOLAR_TIME = 101;
	final static public int SUNRISE_TIME = 102;
	final static public int SUNSET_TIME = 103;
	/*
	 * Longitud máxima de la sombra que vamos a permitir.
	 */
	static final public float MAX_SHADOW_LENGT = 1000f;

	static public int DEFAULT_TIME_WINDOW = 4 * 60;
	static public double DEFAULT_LATITUDE  = 37.36d;
	static public double DEFAULT_LONGITUDE = -5.97d;
	Calendar calendar = new GregorianCalendar();//Calendar.getInstance();
	float time_zone   = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 3600000.0f; // 1000 * 60 * 60
	
	double latitude   = 37.36d;
	double longitude  = -5.97d;
	int time_window = 24*60; //En minutos

	boolean recalculate = true;
	double declination = Double.NEGATIVE_INFINITY;
	geomagModel model;
	
	public SolarInformation() {
		now();
		latitude   = DEFAULT_LATITUDE;
		longitude  = DEFAULT_LONGITUDE;
		time_window = DEFAULT_TIME_WINDOW;
		model = new geomagModel();
	}
	
	synchronized public void setTimeZone( float timeZone ) {
		time_zone = timeZone;
		recalculate = true;
	}

	synchronized public void setTimeWindow( int minutes ) {
		time_window = minutes;
	}
	
	synchronized public void setEndTime( int hour, int minute ) {
		time_window = (hour*60+minute)-(calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE));
		if ( time_window < 0 )
			time_window = DEFAULT_TIME_WINDOW;
	}
	
	synchronized public void setPosition( double latitude, double longitude ) {
		this.latitude = latitude;
		this.longitude = longitude;
		recalculate = true;
	}
	
	synchronized public void setDate( int year, int month, int day ) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		declination = Double.NEGATIVE_INFINITY;
		recalculate = true;
	}
	
	synchronized public void setTime( int hour, int minute ) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		recalculate = true;
	}
	
	synchronized public void now() {
		calendar = new GregorianCalendar();
		time_zone   = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 3600000.0f; // 1000 * 60 * 60
		declination = Double.NEGATIVE_INFINITY;
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
			res = time_window;
			break;
		case DECLINATION_VALUE:
			res = declination;
			break;
		case LATITUDE_VALUE:
			res = latitude;
			break;
		case LONGITUDE_VALUE:
			res = longitude;
			break;
		default:
			break;
		}
		return res;
	}

	/*
	 * agalan: He modificado este método para que en vez de String devuelva un java.util.Date
	 */
	synchronized public Date getTime(int field) {
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
		int t = (int) Math.floor(res);
		int m = (int) Math.floor((res - t) * 60.0);
		
		//if (m < 10) minute = "0" + m; else minute = ""+m;
		//if (t < 10) hour = "0" + t; else hour = ""+t;
		//return hour + ":" + minute;
		
		Date date = new Date();
		date.setHours(t);
		date.setMinutes(m);
		return date;
		
	}
	
	synchronized public String getStringTime(int field) {
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
		int t = (int) Math.floor(res);
		int m = (int) Math.floor((res - t) * 60.0);
		
		String minute, hour;
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

	/*
	 * agalan: modificado este método para que en vez de azimut y longitud de sobra
	 * devuelva la X y la Y
	 */
/*	synchronized public float[] calculateShadowsRange( float[] shadows, int step ) {
		int size = (int)Math.round( time_window_minutes / step + 0.5);
		if ( shadows == null || (shadows.length / 2) != size )
			shadows = new float[size*2];
		calendar.add(Calendar.MINUTE, size*step);
		for ( int n = size-1; n >= 0; n-- ) {
			calendar.add( Calendar.MINUTE , -1*step);
			compute( latitude, longitude, time_zone, calendar );
//			shadows[n*2] = -(float)(Math.sin(azimut*Math.PI/180)*(float)getValue(SolarInformation.AZIMUT_VALUE));
//			shadows[n*2+1] = -(float)(Math.cos(azimut*Math.PI/180)*(float)getValue(SolarInformation.SHADOW_LENGTH_VALUE));
			shadows[n*2]= -(float)(Math.sin(getValue(SolarInformation.AZIMUT_VALUE)*Math.PI/180)*getValue(SolarInformation.SHADOW_LENGTH_VALUE));
			shadows[n*2+1]=-(float)(Math.cos(getValue(SolarInformation.AZIMUT_VALUE)*Math.PI/180)*getValue(SolarInformation.SHADOW_LENGTH_VALUE));
		}
		
		for(int i=0;i<shadows.length;i=i+2){
			System.out.println(shadows[i]+" , "+shadows[i+1]);
		}
		return shadows;
	}*/


	
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

		if ( fSunrise >= fLocalTime || fLocalTime >= fSunset || fShadowLength > 30 )
			fShadowLength = 0f;

		if ( declination == Double.NEGATIVE_INFINITY )
			declination = model.getDeclination(latitude, longitude, instant);

		recalculate = false;
	}
	
	
	
    synchronized private float[] calculatePointShadow( float rotation ){
        float puntos[] = new float[2]; 

        double azimut=getValue(AZIMUT_VALUE);
        double shadowLength=getValue(SHADOW_LENGTH_VALUE);
        float ang = Utils.normDegrees((float)azimut-rotation);
        float rad = (float)Utils.deg2rad(ang);
        puntos[0]=-(float)(Math.sin(rad)*shadowLength);
        puntos[1]=-(float)(Math.cos(rad)*shadowLength);
        return puntos;

    }

	
	float[] stripShadow=new float[12];
	
	synchronized public float[] calculateRectangleShadow(float rotation,
			float offsetX, float offsetY) {
		/*
		 * La franja de sombra será descrita por cuatro puntos, el origen, la
		 * sombra en el instante pasado como parámetro y dos puntos que
		 * representan la sombra 5 minutos antes y después del instante pasado
		 * como parámetro.
		 */

		float xy[] = calculatePointShadow(rotation);

//		double sunrise = this.getValue(SUNRISE_VALUE);
//		double sunset = this.getValue(SUNSET_VALUE);
//		double localtime = this.getValue(LOCAL_VALUE);
		
//		if ( sunrise >= localtime || localtime >= sunset ) {
		if ( xy[0] == 0 && xy[1] == 0 ) {
			return xy;
		}
		
		if (xy[0] > 0 && xy[1] > 0) {
			stripShadow[0] = offsetX;
			stripShadow[1] = -offsetY;

			stripShadow[2] = 0F;
			stripShadow[3] = 0F;

			stripShadow[4] = -offsetX;
			stripShadow[5] = offsetY;

			stripShadow[6] = xy[0] + -offsetX;
			stripShadow[7] = xy[1] + offsetY;

			stripShadow[8] = xy[0] + offsetX;
			stripShadow[9] = xy[1] + offsetY;

			stripShadow[10] = xy[0] + offsetX;
			stripShadow[11] = xy[1] + -offsetY;

		} else if (xy[0] > 0 && xy[1] < 0) {
			stripShadow[0] = offsetX;
			stripShadow[1] = offsetY;

			stripShadow[2] = 0F;
			stripShadow[3] = 0F;

			stripShadow[4] = -offsetX;
			stripShadow[5] = -offsetY;

			stripShadow[6] = xy[0] + -offsetX;
			stripShadow[7] = xy[1] + -offsetY;

			stripShadow[8] = xy[0] + offsetX;
			stripShadow[9] = xy[1] + -offsetY;

			stripShadow[10] = xy[0] + offsetX;
			stripShadow[11] = xy[1] + offsetY;

		} else if (xy[0] < 0 && xy[1] < 0) {
			stripShadow[0] = offsetX;
			stripShadow[1] = -offsetY;

			stripShadow[2] = 0F;
			stripShadow[3] = 0F;

			stripShadow[4] = -offsetX;
			stripShadow[5] = offsetY;

			stripShadow[6] = xy[0] + -offsetX;
			stripShadow[7] = xy[1] + offsetY;

			stripShadow[8] = xy[0] + -offsetX;
			stripShadow[9] = xy[1] + -offsetY;

			stripShadow[10] = xy[0] + offsetX;
			stripShadow[11] = xy[1] + -offsetY;

		} else if (xy[0] < 0 && xy[1] > 0) {
			stripShadow[0] = offsetX;
			stripShadow[1] = offsetY;

			stripShadow[2] = 0F;
			stripShadow[3] = 0F;

			stripShadow[4] = -offsetX;
			stripShadow[5] = -offsetY;

			stripShadow[6] = xy[0] + -offsetX;
			stripShadow[7] = xy[1] + -offsetY;

			stripShadow[8] = xy[0] + -offsetX;
			stripShadow[9] = xy[1] + offsetY;

			stripShadow[10] = xy[0] + offsetX;
			stripShadow[11] = xy[1] + offsetY;
		}

		return stripShadow;
	}

	
//	synchronized public float[] calculateRectangleShadow(Calendar instant,
//			float rotation, float offsetX, float offsetY) {
//		/*
//		 * La franja de sombra será descrita por cuatro puntos, el origen, la
//		 * sombra en el instante pasado como parámetro y dos puntos que
//		 * representan la sombra 5 minutos antes y después del instante pasado
//		 * como parámetro.
//		 */
//
//		float[] punto1 = new float[] { offsetX, offsetY };
//		float[] punto2 = new float[] { offsetX, -offsetY };
//		float[] punto3 = new float[] { -offsetX, -offsetY };
//		float[] punto4 = new float[] { -offsetX, offsetY };
//
//		float xy[] = calculatePointShadow(rotation);
//
//		// SUNSET:Actualizo la hora de la puesta de SOL
//		TimeZone tz = instant.getTimeZone();
//		double time = this.getValue(SUNSET_VALUE);
//		int t = (int) Math.floor(time);
//		int m = (int) Math.floor((time - t) * 60.0);
//		Calendar sunsetDate = new GregorianCalendar(instant.get(Calendar.YEAR),
//				instant.get(Calendar.MONTH),
//				instant.get(Calendar.DAY_OF_MONTH), t, m - 5);
//		sunsetDate.setTimeZone(tz);
//		// SUNRISE:Actualizo la hora de la salida del SOL
//		time = this.getValue(SUNRISE_VALUE);
//		t = (int) Math.floor(time);
//		m = (int) Math.floor((time - t) * 60.0);
//		Calendar sunriseDate = new GregorianCalendar(
//				instant.get(Calendar.YEAR), instant.get(Calendar.MONTH),
//				instant.get(Calendar.DAY_OF_MONTH), t, m + 5);
//		sunriseDate.setTimeZone(tz);
//		if (instant.before(sunriseDate) || instant.after(sunsetDate)) {
//			return new float[] { 0f, 0f };
//		}
//		if (xy[0] > 0 && xy[1] > 0) {
//			stripShadow[0] = punto2[0];
//			stripShadow[1] = punto2[1];
//
//			stripShadow[2] = 0F;
//			stripShadow[3] = 0F;
//
//			stripShadow[4] = punto4[0];
//			stripShadow[5] = punto4[1];
//
//			stripShadow[6] = xy[0] + punto4[0];
//			stripShadow[7] = xy[1] + punto4[1];
//
//			stripShadow[8] = xy[0] + punto1[0];
//			stripShadow[9] = xy[1] + punto1[1];
//
//			stripShadow[10] = xy[0] + punto2[0];
//			stripShadow[11] = xy[1] + punto2[1];
//
//		} else if (xy[0] > 0 && xy[1] < 0) {
//			stripShadow[0] = punto1[0];
//			stripShadow[1] = punto1[1];
//
//			stripShadow[2] = 0F;
//			stripShadow[3] = 0F;
//
//			stripShadow[4] = punto3[0];
//			stripShadow[5] = punto3[1];
//
//			stripShadow[6] = xy[0] + punto3[0];
//			stripShadow[7] = xy[1] + punto3[1];
//
//			stripShadow[8] = xy[0] + punto2[0];
//			stripShadow[9] = xy[1] + punto2[1];
//
//			stripShadow[10] = xy[0] + punto1[0];
//			stripShadow[11] = xy[1] + punto1[1];
//
//		} else if (xy[0] < 0 && xy[1] < 0) {
//			stripShadow[0] = punto2[0];
//			stripShadow[1] = punto2[1];
//
//			stripShadow[2] = 0F;
//			stripShadow[3] = 0F;
//
//			stripShadow[4] = punto4[0];
//			stripShadow[5] = punto4[1];
//
//			stripShadow[6] = xy[0] + punto4[0];
//			stripShadow[7] = xy[1] + punto4[1];
//
//			stripShadow[8] = xy[0] + punto3[0];
//			stripShadow[9] = xy[1] + punto3[1];
//
//			stripShadow[10] = xy[0] + punto2[0];
//			stripShadow[11] = xy[1] + punto2[1];
//
//		} else if (xy[0] < 0 && xy[1] > 0) {
//			stripShadow[0] = punto1[0];
//			stripShadow[1] = punto1[1];
//
//			stripShadow[2] = 0F;
//			stripShadow[3] = 0F;
//
//			stripShadow[4] = punto3[0];
//			stripShadow[5] = punto3[1];
//
//			stripShadow[6] = xy[0] + punto3[0];
//			stripShadow[7] = xy[1] + punto3[1];
//
//			stripShadow[8] = xy[0] + punto4[0];
//			stripShadow[9] = xy[1] + punto4[1];
//
//			stripShadow[10] = xy[0] + punto1[0];
//			stripShadow[11] = xy[1] + punto1[1];
//		}
//
//		return stripShadow;
//	}
	
	

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
	
	/*
	 * Un método de prueba que devuelve en un array de floats  que representan una cuarta de circunferencia
	 */
	public float[] calculaSombraTest(){
		
		float[] puntos2=new float[36];
		puntos2[0]=puntos2[1]=0;
		float angle=0;
		for(int i=0;i<17;i++){
			angle=(float)((Math.PI/32)*i);
			float x=(float)(5*Math.cos(angle));
			float y=(float)(5*Math.sin(angle));
			puntos2[2*i+2]=(x<0.1f?0:x);
			puntos2[2*i+3]=(y<0.1f?0:y);
		}

		return puntos2;
	}

	
	
}
