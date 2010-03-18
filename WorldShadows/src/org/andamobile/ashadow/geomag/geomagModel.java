package org.andamobile.ashadow.geomag;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.util.Log;

public class geomagModel {

	public static final int MAX_MODEL_DEGREE = 13;
	public static final int MODEL_ALLOC_SIZE = 14;
	private static final double toRadians = 0.01745329252D;
	@SuppressWarnings("unused")
	private static final double GoldenLat = 39.75D;
	@SuppressWarnings("unused")
	private static final double GoldenLon = 105.20829999999999D;
	private static final double gmYearMin = 1600D;
	private static final double gmYearMax = 2050D;
	private static final double gmElevMinM = -200000D;
	private static final double gmElevMaxM = 1000000D;
	@SuppressWarnings("unused")
	private static final double E_RADIUS_KM_EQUAT = 6378.1369999999997D;
	@SuppressWarnings("unused")
	private static final double E_RADIUS_KM_POLAR = 6356.7523142D;
	private static final double re = 6371.1999999999998D;
	private static final double E_RAD_EQUA_SQUARE = 40680631.590768993D;
	private static final double E_RAD_POLE_SQUARE = 40408299.984087057D;
	private static final double E_RAD_DIFF_SQUARE = 272331.60668193549D;
	private static final double E_RAD_EQUA_QUAD = 1654913786623872.2D;
	@SuppressWarnings("unused")
	private static final double E_RAD_POLE_QUAD = 1632830707603970D;
	private static final double E_RAD_DIFF_QUAD = 22083079019902.25D;
	boolean myStatus;
	String statusString;
	boolean releaseObjects;
	BufferedReader myReader;
	boolean pointValid[] = { true, true, true };
	double modelData[][][];
	String modelSrc;
	double epoch;
	double lifeSpan;
	int modelDegree;
	gmModelAreas modelScope;
	double results[][];
	fourDPoint loPoint;
	fourDPoint hiPoint;
	fourDPoint defPoint;
	fourDPoint calcPoint;
	private double k[][];
	private double fn[];
	private double fm[];
	private double snorm[];
	private double sp[];
	private double cp[];
	private double dp[][];
	private double tc[][];
	private double pp[];
	private double oldLat;
	private double oldLon;
	private double oldAlt;
	private double oldTime;
	private double rlon;
	private double rlat;
	private double srlon;
	private double srlat;
	private double crlon;
	private double crlat;
	private double srlat2;
	private double crlat2;
	private double q;
	private double q1;
	private double q2;
	private double ct;
	private double st;
	private double r2;
	private double r;
	private double d;
	private double ca;
	private double sa;
	boolean running;
	boolean cancelling;

	public geomagModel() {
		
        myStatus = false;
        releaseObjects = false;
        myReader = null;
        modelData = new double[14][14][2];
        epoch = 0.0D;
        lifeSpan = 5D;
        modelDegree = 0;
        modelScope = gmModelAreas.INVALID;
        results = new double[8][2];
        loPoint = null;
        hiPoint = null;
        defPoint = null;
        calcPoint = null;
        k = new double[14][14];
        fn = new double[14];
        fm = new double[14];
        snorm = new double[196];
        sp = new double[14];
        cp = new double[14];
        dp = new double[14][14];
        tc = new double[14][14];
        pp = new double[14];
        oldLat = (0.0D / 0.0D);
        oldLon = (0.0D / 0.0D);
        oldAlt = (0.0D / 0.0D);
        oldTime = (0.0D / 0.0D);
        running = false;
        cancelling = false;
        modelSrc = "";
        myStatus = false;
        statusString = "geomagModel Uninitialized";

		
		myReader = new BufferedReader( new StringReader( model) );
		parseModelSource( myReader );
		try {
			myReader.close();
		} catch (IOException e) {
		}
		
//		calcPassResult firstPass = calculatePass( 37.36d, -5.97d, 0, 2009.6356 );
//        results[0][0] = firstPass.ti;
//        results[1][0] = firstPass.ti * Math.cos(toRadians * firstPass.dec) * Math.cos(toRadians * firstPass.dip);
//        results[2][0] = firstPass.ti * Math.cos(toRadians * firstPass.dip) * Math.sin(toRadians * firstPass.dec);
//        results[3][0] = firstPass.ti * Math.sin(toRadians * firstPass.dip);
//        results[4][0] = firstPass.ti * Math.cos(toRadians * firstPass.dip);
//        results[5][0] = firstPass.dec;
//        results[6][0] = firstPass.dip;
//        results[7][0] = firstPass.gv;
        
//		Log.i("DECLINATION", ""+results[5][0]);
	}
	
	public double getDeclination( double lat, double lon, Calendar date ) {
		calcPassResult res = calculatePass( lat, lon, 0, date.get(Calendar.YEAR) + Math.round(date.get(Calendar.DAY_OF_YEAR)/365f*1000f)/1000f );
		Log.i("DECLINATION", "" + res.dec);
		return res.dec;
		
	}
	
	private class calcPassResult {

		public double dec;
		public double dip;
		public double ti;
		public double gv;

		calcPassResult(double a, double b, double c, double d) {
			dec = 0.0D;
			dip = 0.0D;
			ti = 0.0D;
			gv = 0.0D;
			dec = a;
			dip = b;
			ti = c;
			gv = d;
		}
	}

	boolean parseModelSource(BufferedReader rdr) {
		boolean retStatus = false;
		epoch = 0.0D;
		lifeSpan = 5D;
		modelDegree = 0;
		modelScope = gmModelAreas.INVALID;
		loPoint = null;
		hiPoint = null;
		defPoint = null;
		try {
			StringTokenizer Header = new StringTokenizer(rdr.readLine());
			int tokenCount = Header.countTokens();
			if (tokenCount < 2 || 8 < tokenCount)
				throw new badLocationFormat("Invalid Model file header line");
			epoch = Double.valueOf(Header.nextToken()).doubleValue();
			if (epoch < gmYearMin || gmYearMax < epoch)
				throw new badLocationFormat(
						"Invalid Model file: epoch out of range");
			String wrkToken = Header.nextToken();
			if (tokenCount == 2) {
				modelScope = gmModelAreas.GLOBAL;
				loPoint = new fourDPoint(-90D, 0.0D, gmElevMinM, epoch);
				hiPoint = new fourDPoint(90D, 360D, gmElevMaxM, epoch + lifeSpan);
			}
			if (tokenCount > 2) {
				wrkToken = Header.nextToken();
				if (wrkToken.indexOf('/') < 0)
					lifeSpan = Double.valueOf(wrkToken).doubleValue();
				if (tokenCount == 8)
					lifeSpan = Double.valueOf(Header.nextToken()).doubleValue();
				if (tokenCount > 3) {
					modelScope = gmModelAreas.REGION;
					double loLat = Double.valueOf(Header.nextToken())
							.doubleValue();
					double hiLat = Double.valueOf(Header.nextToken())
							.doubleValue();
					double loLon = Double.valueOf(Header.nextToken())
							.doubleValue();
					double hiLon = Double.valueOf(Header.nextToken())
							.doubleValue();
					loPoint = new fourDPoint(loLat, loLon, gmElevMinM, epoch);
					hiPoint = new fourDPoint(hiLat, hiLon, gmElevMaxM, epoch
							+ lifeSpan);
				} else {
					loPoint = new fourDPoint(-90D, 0.0D, gmElevMinM, epoch);
					hiPoint = new fourDPoint(90D, 360D, gmElevMaxM, epoch
							+ lifeSpan);
					modelScope = gmModelAreas.GLOBAL;
				}
			}
			int wrkDeg = 0;
			int wrkOrd = 0;
			int lastDeg = 0;
			do {
				String wrkLine;
				if ((wrkLine = rdr.readLine()) == null
						|| wrkLine.substring(0, 4).equals("9999"))
					break;
				if (wrkLine.length() < 48)
					throw new badLocationFormat("Invalid data line too short");
				wrkDeg = Integer.parseInt(wrkLine.substring(0, 3).trim());
				if (wrkDeg < 1 || 13 < wrkDeg)
					throw new badLocationFormat(
							"Invalid data line Degree out of range 1-"
									+ String.valueOf(13));
				if (wrkDeg != lastDeg) {
					if (lastDeg != 0 && wrkOrd != lastDeg)
						throw new badLocationFormat(String
								.valueOf((new StringBuffer(
										"Missing data line deg=")).append(
										String.valueOf(lastDeg)).append(
										", order=").append(
										String.valueOf(wrkOrd))));
					lastDeg = wrkDeg;
				}
				wrkOrd = Integer.parseInt(wrkLine.substring(3, 6).trim());
				if (wrkOrd < 0 || wrkDeg < wrkOrd)
					throw new badLocationFormat(
							"Invalid data line Degree out of range 1-"
									+ String.valueOf(13));
				modelData[wrkOrd][wrkDeg][0] = Double.valueOf(
						wrkLine.substring(7, 16).trim()).doubleValue();
				modelData[wrkOrd][wrkDeg][1] = Double.valueOf(
						wrkLine.substring(28, 37).trim()).doubleValue();
				if (wrkOrd > 0) {
					modelData[wrkDeg][wrkOrd - 1][0] = Double.valueOf(
							wrkLine.substring(17, 26).trim()).doubleValue();
					modelData[wrkDeg][wrkOrd - 1][1] = Double.valueOf(
							wrkLine.substring(38, 48).trim()).doubleValue();
				}
			} while (true);
			modelData[0][0][0] = 0.0D;
			modelData[0][0][1] = 0.0D;
			modelDegree = wrkDeg;
			snorm[0] = 1.0D;
			cp[0] = 1.0D;
			pp[0] = 1.0D;
			sp[0] = 0.0D;
			dp[0][0] = 0.0D;
			fn[0] = 1.0D;
			fm[0] = 0.0D;
			k[0][0] = 0.0D;
			k[1][1] = 0.0D;
			for (int Degr = 1; Degr <= modelDegree; Degr++) {
				snorm[Degr] = (snorm[Degr - 1] * (double) (2 * Degr - 1))
						/ (double) Degr;
				double j = 2D;
				int Ordr = 0;
				for (int D2 = Degr + 1; D2 > 0;) {
					k[Ordr][Degr] = (double) ((Degr - 1) * (Degr - 1) - Ordr
							* Ordr)
							/ (double) ((2 * Degr - 1) * (2 * Degr - 3));
					if (Ordr > 0) {
						snorm[Degr + Ordr * 13] = snorm[Degr + (Ordr - 1) * 13]
								* Math.sqrt(((double) ((Degr - Ordr) + 1) * j)
										/ (double) (Degr + Ordr));
						j = 1.0D;
						modelData[Degr][Ordr - 1][0] *= snorm[Degr + Ordr * 13];
						modelData[Degr][Ordr - 1][1] *= snorm[Degr + Ordr * 13];
					}
					modelData[Ordr][Degr][0] *= snorm[Degr + Ordr * 13];
					modelData[Ordr][Degr][1] *= snorm[Degr + Ordr * 13];
					D2--;
					Ordr++;
				}

				fn[Degr] = Degr + 1;
				fm[Degr] = Degr;
			}

			k[1][1] = 0.0D;
			retStatus = true;
		} catch (badLocationFormat blf) {
			myStatus = false;
			statusString = blf.getMessage();
		} catch (NumberFormatException nfx) {
			myStatus = false;
			statusString = "geomagModel.parseSource(): number format exception";
		} catch (IOException iox) {
			statusString = "Error while reading stream";
			myStatus = false;
		} catch (Exception ex) {
			statusString
					.concat("geomagModel.parseSource(): Unidentified exception");
			myStatus = false;
		}
		return retStatus;
	}

	private calcPassResult calculatePass(double glat, double glon, double alt,
			double time) {
		double dt = time - epoch;
		if (glat != oldLat || alt != oldAlt) {
			rlat = toRadians * glat;
			srlat = Math.sin(rlat);
			crlat = Math.cos(rlat);
			srlat2 = srlat * srlat;
			crlat2 = crlat * crlat;
			q = Math.sqrt(E_RAD_EQUA_SQUARE - E_RAD_DIFF_SQUARE * srlat2);
			q1 = alt * q;
			q2 = ((q1 + E_RAD_EQUA_SQUARE) / (q1 + E_RAD_POLE_SQUARE))
					* ((q1 + E_RAD_EQUA_SQUARE) / (q1 + E_RAD_POLE_SQUARE));
			ct = srlat / Math.sqrt(q2 * crlat2 + srlat2);
			st = Math.sqrt(1.0D - ct * ct);
			r2 = alt * alt + 2D * q1
					+ (E_RAD_EQUA_QUAD - E_RAD_DIFF_QUAD * srlat2)
					/ (q * q);
			r = Math.sqrt(r2);
			d = Math.sqrt(E_RAD_EQUA_SQUARE * crlat2 + E_RAD_POLE_SQUARE
					* srlat2);
			ca = (alt + d) / r;
			sa = (E_RAD_DIFF_SQUARE * crlat * srlat) / (r * d);
		}
		if (glon != oldLon) {
			rlon = toRadians * glon;
			srlon = Math.sin(rlon);
			crlon = Math.cos(rlon);
			sp[1] = srlon;
			cp[1] = crlon;
			for (int m = 2; m <= modelDegree; m++) {
				sp[m] = sp[1] * cp[m - 1] + cp[1] * sp[m - 1];
				cp[m] = cp[1] * cp[m - 1] - sp[1] * sp[m - 1];
			}

		}
		double br = 0.0D;
		double bt = 0.0D;
		double bp = 0.0D;
		double bpp = 0.0D;
		double aor = re / r;
		double ar = aor * aor;
		for (int n = 1; n <= modelDegree; n++) {
			ar *= aor;
			int m = 0;
			for (int D4 = n + 1; D4 > 0;) {
				System.out.print("");
				if (alt != oldAlt || glat != oldLat)
					if (n == m) {
						snorm[n + m * 13] = st * snorm[(n - 1) + (m - 1) * 13];
						dp[m][n] = st * dp[m - 1][n - 1] + ct
								* snorm[(n - 1) + (m - 1) * 13];
					} else if (n == 1 && m == 0) {
						snorm[n + m * 13] = ct * snorm[(n - 1) + m * 13];
						dp[m][n] = ct * dp[m][n - 1] - st
								* snorm[(n - 1) + m * 13];
					} else if (n > 1 && n != m) {
						if (m > n - 2) {
							snorm[(n - 2) + m * 13] = 0.0D;
							dp[m][n - 2] = 0.0D;
						}
						snorm[n + m * 13] = ct * snorm[(n - 1) + m * 13]
								- k[m][n] * snorm[(n - 2) + m * 13];
						dp[m][n] = ct * dp[m][n - 1] - st
								* snorm[(n - 1) + m * 13] - k[m][n]
								* dp[m][n - 2];
					}
				if (time != oldTime) {
					tc[m][n] = modelData[m][n][0] + dt * modelData[m][n][1];
					if (m > 0)
						tc[n][m - 1] = modelData[n][m - 1][0] + dt
								* modelData[n][m - 1][1];
				}
				double par = ar * snorm[n + m * 13];
				double temp1;
				double temp2;
				if (m == 0) {
					temp1 = tc[m][n] * cp[m];
					temp2 = tc[m][n] * sp[m];
				} else {
					temp1 = tc[m][n] * cp[m] + tc[n][m - 1] * sp[m];
					temp2 = tc[m][n] * sp[m] - tc[n][m - 1] * cp[m];
				}
				bt -= ar * temp1 * dp[m][n];
				bp += fm[m] * temp2 * par;
				br += fn[n] * temp1 * par;
				if (st == 0.0D && m == 1) {
					if (n == 1)
						pp[n] = pp[n - 1];
					else
						pp[n] = ct * pp[n - 1] - k[m][n] * pp[n - 2];
					double parp = ar * pp[n];
					bpp += fm[m] * temp2 * parp;
				}
				D4--;
				m++;
			}

		}

		if (st == 0.0D)
			bp = bpp;
		else
			bp /= st;
		double bx = -bt * ca - br * sa;
		double by = bp;
		double bz = bt * sa - br * ca;
		double bh = Math.sqrt(bx * bx + by * by);
		double tiRet = Math.sqrt(bh * bh + bz * bz);
		double decRet = Math.atan2(by, bx) / toRadians;
		double dipRet = Math.atan2(bz, bh) / toRadians;
		double gvRet = (0.0D / 0.0D);
		if (Math.abs(glat) >= 55D) {
			if (glat > 0.0D && glon >= 0.0D)
				gvRet = decRet - glon;
			if (glat > 0.0D && glon < 0.0D)
				gvRet = decRet + Math.abs(glon);
			if (glat < 0.0D && glon >= 0.0D)
				gvRet = decRet + glon;
			if (glat < 0.0D && glon < 0.0D)
				gvRet = decRet - Math.abs(glon);
			if (gvRet > 180D)
				gvRet -= 360D;
			if (gvRet < -180D)
				gvRet += 360D;
		}
		oldLat = glat;
		oldLon = glon;
		oldAlt = alt;
		oldTime = time;
		return new calcPassResult(decRet, dipRet, tiRet, gvRet);
	}

	String model = 
		"    2005.00        IGRF-2005            12/22/04\n"+
		"  1  0  -29556.8       0.0        8.8        0.0\n"+
		"  1  1   -1671.8    5080.0       10.8      -21.3\n"+
		"  2  0   -2340.5       0.0      -15.0        0.0\n"+
		"  2  1    3047.0   -2594.9       -6.9      -23.3\n"+
		"  2  2    1656.9    -516.7       -1.0      -14.0\n"+
		"  3  0    1335.7       0.0       -0.3        0.0\n"+
		"  3  1   -2305.3    -200.4       -3.1        5.4\n"+
		"  3  2    1246.8     269.3       -0.9       -6.5\n"+
		"  3  3     674.4    -524.5       -6.8       -2.0\n"+
		"  4  0     919.8       0.0       -2.5        0.0\n"+
		"  4  1     798.2     281.4        2.8        2.0\n"+
		"  4  2     211.5    -225.8       -7.1        1.8\n"+
		"  4  3    -379.5     145.7        5.9        5.6\n"+
		"  4  4     100.2    -304.7       -3.2        0.0\n"+
		"  5  0    -227.6       0.0       -2.6        0.0\n"+
		"  5  1     354.4      42.7        0.4        0.1\n"+
		"  5  2     208.8     179.8       -3.0        1.8\n"+
		"  5  3    -136.6    -123.0       -1.2        2.0\n"+
		"  5  4    -168.3     -19.5        0.2        4.5\n"+
		"  5  5     -14.1     103.6       -0.6       -1.0\n"+
		"  6  0      72.9       0.0       -0.8        0.0\n"+
		"  6  1      69.6     -20.2        0.2       -0.4\n"+
		"  6  2      76.6      54.7       -0.2       -1.9\n"+
		"  6  3    -151.1      63.7        2.1       -0.4\n"+
		"  6  4     -15.0     -63.4       -2.1       -0.4\n"+
		"  6  5      14.7       0.0       -0.4       -0.2\n"+
		"  6  6     -86.4      50.3        1.3        0.9\n"+
		"  7  0      79.8       0.0       -0.4        0.0\n"+
		"  7  1     -74.4     -61.4        0.0        0.8\n"+
		"  7  2      -1.4     -22.5       -0.2        0.4\n"+
		"  7  3      38.6       6.9        1.1        0.1\n"+
		"  7  4      12.3      25.4        0.6        0.2\n"+
		"  7  5       9.4      10.9        0.4       -0.9\n"+
		"  7  6       5.5     -26.4       -0.5       -0.3\n"+
		"  7  7       2.0      -4.8        0.9        0.3\n"+
		"  8  0      24.8       0.0       -0.2        0.0\n"+
		"  8  1       7.7      11.2        0.2       -0.2\n"+
		"  8  2     -11.4     -21.0       -0.2        0.2\n"+
		"  8  3      -6.8       9.7        0.2        0.2\n"+
		"  8  4     -18.0     -19.8       -0.2        0.4\n"+
		"  8  5      10.0      16.1        0.2        0.2\n"+
		"  8  6       9.4       7.7        0.5       -0.3\n"+
		"  8  7     -11.4     -12.8       -0.7        0.5\n"+
		"  8  8      -5.0      -0.1        0.5        0.4\n"+
		"  9  0       5.6       0.0        0.0        0.0\n"+
		"  9  1       9.8     -20.1        0.0        0.0\n"+
		"  9  2       3.6      12.9        0.0        0.0\n"+
		"  9  3      -7.0      12.7        0.0        0.0\n"+
		"  9  4       5.0      -6.7        0.0        0.0\n"+
		"  9  5     -10.8      -8.1        0.0        0.0\n"+
		"  9  6      -1.3       8.1        0.0        0.0\n"+
		"  9  7       8.7       2.9        0.0        0.0\n"+
		"  9  8      -6.7      -7.9        0.0        0.0\n"+
		"  9  9      -9.2       5.9        0.0        0.0\n"+
		" 10  0      -2.2       0.0        0.0        0.0\n"+
		" 10  1      -6.3       2.4        0.0        0.0\n"+
		" 10  2       1.6       0.2        0.0        0.0\n"+
		" 10  3      -2.5       4.4        0.0        0.0\n"+
		" 10  4      -0.1       4.7        0.0        0.0\n"+
		" 10  5       3.0      -6.5        0.0        0.0\n"+
		" 10  6       0.3      -1.0        0.0        0.0\n"+
		" 10  7       2.1      -3.4        0.0        0.0\n"+
		" 10  8       3.9      -0.9        0.0        0.0\n"+
		" 10  9      -0.1      -2.3        0.0        0.0\n"+
		" 10 10      -2.2      -8.0        0.0        0.0\n"+
		" 11  0       2.9       0.0        0.0        0.0\n"+
		" 11  1      -1.6       0.3        0.0        0.0\n"+
		" 11  2      -1.7       1.4        0.0        0.0\n"+
		" 11  3       1.5      -0.7        0.0        0.0\n"+
		" 11  4      -0.2      -2.4        0.0        0.0\n"+
		" 11  5       0.2       0.9        0.0        0.0\n"+
		" 11  6      -0.7      -0.6        0.0        0.0\n"+
		" 11  7       0.5      -2.7        0.0        0.0\n"+
		" 11  8       1.8      -1.0        0.0        0.0\n"+
		" 11  9       0.1      -1.5        0.0        0.0\n"+
		" 11 10       1.0      -2.0        0.0        0.0\n"+
		" 11 11       4.1      -1.4        0.0        0.0\n"+
		" 12  0      -2.2       0.0        0.0        0.0\n"+
		" 12  1      -0.3      -0.5        0.0        0.0\n"+
		" 12  2       0.3       0.3        0.0        0.0\n"+
		" 12  3       0.9       2.3        0.0        0.0\n"+
		" 12  4      -0.4      -2.7        0.0        0.0\n"+
		" 12  5       1.0       0.6        0.0        0.0\n"+
		" 12  6      -0.4       0.4        0.0        0.0\n"+
		" 12  7       0.5       0.0        0.0        0.0\n"+
		" 12  8      -0.3       0.0        0.0        0.0\n"+
		" 12  9      -0.4       0.3        0.0        0.0\n"+
		" 12 10       0.0      -0.8        0.0        0.0\n"+
		" 12 11      -0.4      -0.4        0.0        0.0\n"+
		" 12 12       0.0       1.0        0.0        0.0\n"+
		" 13  0      -0.2       0.0        0.0        0.0\n"+
		" 13  1      -0.9      -0.7        0.0        0.0\n"+
		" 13  2       0.3       0.3        0.0        0.0\n"+
		" 13  3       0.3       1.7        0.0        0.0\n"+
		" 13  4      -0.4      -0.5        0.0        0.0\n"+
		" 13  5       1.2      -1.0        0.0        0.0\n"+
		" 13  6      -0.4       0.0        0.0        0.0\n"+
		" 13  7       0.7       0.7        0.0        0.0\n"+
		" 13  8      -0.3       0.2        0.0        0.0\n"+
		" 13  9       0.4       0.6        0.0        0.0\n"+
		" 13 10      -0.1       0.4        0.0        0.0\n"+
		" 13 11       0.4      -0.2        0.0        0.0\n"+
		" 13 12      -0.1      -0.5        0.0        0.0\n"+
		" 13 13      -0.3      -1.0        0.0        0.0\n"+
		"9999\n"+
		"9999\n";
}
