package zejfseis4.utils;

public interface GeoUtils {
	public static final double EARTH_CIRCUMFERENCE = 40082;
	public static final double EARTH_RADIUS = EARTH_CIRCUMFERENCE / (2 * Math.PI);// 6371.0;

	/*public static final double ZEJF_LAT = 50.262;
	public static final double ZEJF_LON = 17.262;*/

	public static double[] moveOnGlobe(double lat1, double long1, double distance, double angle) {
		// calculate angles
		double delta = distance / EARTH_RADIUS;
		double theta = Math.toRadians(lat1);
		double phi = Math.toRadians(long1);
		double gamma = Math.toRadians(angle);

		// calculate sines and cosines
		double c_theta = Math.cos(theta);
		double s_theta = Math.sin(theta);
		double c_phi = Math.cos(phi);
		double s_phi = Math.sin(phi);
		double c_delta = Math.cos(delta);
		double s_delta = Math.sin(delta);
		double c_gamma = Math.cos(gamma);
		double s_gamma = Math.sin(gamma);

		// calculate end vector
		double x = c_delta * c_theta * c_phi - s_delta * (s_theta * c_phi * c_gamma + s_phi * s_gamma);
		double y = c_delta * c_theta * s_phi - s_delta * (s_theta * s_phi * c_gamma - c_phi * s_gamma);
		double z = s_delta * c_theta * c_gamma + c_delta * s_theta;

		// calculate end lat long
		double theta2 = Math.asin(z);
		double phi2 = Math.atan2(y, x);

		return new double[] { Math.toDegrees(theta2), Math.toDegrees(phi2) };
	}

	public static double placeOnSurface(double travelledDistance, double alt1, double alt2) {
		double l = travelledDistance;
		double d = alt1 - alt2;
		double angDiff = (l * 360.0) / EARTH_CIRCUMFERENCE;
		double s2 = l * l - d * d * Math.cos(Math.toRadians(angDiff));
		if (s2 < 0) {
			return 0;
		}
		double s = Math.sqrt(s2);
		return s;
	}

	public static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);
		return EARTH_RADIUS
				* Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
	}

	public static double calculateAngle(double lat1, double lon1, double lat2, double lon2) {
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);
		double dLon = (lon2 - lon1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		double brng = Math.atan2(y, x);

		brng = Math.toDegrees(brng);
		brng = (brng + 360) % 360;
		// brng = 360 - brng;

		return brng;
	}

	public static double geologicalDistance(double lat1, double lon1, double alt1, double lat2, double lon2,
			double alt2) {
		alt1 += EARTH_RADIUS;
		alt2 += EARTH_RADIUS;
		double x1 = Math.sin(Math.toRadians(lon1)) * alt1 * Math.cos(Math.toRadians(lat1));
		double z1 = -Math.cos(Math.toRadians(lon1)) * alt1 * Math.cos(Math.toRadians(lat1));
		double y1 = Math.sin(Math.toRadians(lat1)) * alt1;
		double x2 = Math.sin(Math.toRadians(lon2)) * alt2 * Math.cos(Math.toRadians(lat2));
		double z2 = -Math.cos(Math.toRadians(lon2)) * alt2 * Math.cos(Math.toRadians(lat2));
		double y2 = Math.sin(Math.toRadians(lat2)) * alt2;
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
	}

	public static double pgaFunctionGen1(double mag, double distKm) {
		distKm = Math.abs(distKm);
		double a = mag + 1.5;
		return Math.pow((a + 0.9) / 5.5, 7.0)
				/ (0.09 + Math.pow((distKm * (14.0 - a * 0.85)) / (Math.pow(a, (5.4) / (1 + a * 0.075))), 2.5));
	}

	public static double inversePgaFunctionGen1(double mag, double pga) {
		double a = mag + 1.5;
		return ((Math.pow((Math.pow((a + 0.9) / 5.5, 7.0)) / pga - 0.09, 1 / 2.5)
				* (Math.pow(a, (5.4) / (1 + a * 0.075))))) / (14.0 - a * 0.85);
	}

	public static double detectionNOTCurve_v53(double km) {
		return km <= 1000 ? (Math.log10((km + 18.864036) / 18.864036) * 3) : (Math.log10((km + 0.25) / 0.25) * 1.443);
	}

	public static double probability_v53_dep(double mag, double dist) {
		return Math.pow(10, mag) / Math.pow(10, detectionNOTCurve_v53(dist));
	}

	public static double probability_v6(double mag, double dist, double depth) {
		double actualDetectionCurve = detectionCurve_v6(dist);
		if (dist > 9000) {
			actualDetectionCurve += Math.min(0.3, (dist - 9000) / 2000 * 0.3);
		}
		if (dist > 14700) {
			actualDetectionCurve -= Math.min(0.4, (dist - 14700) / 600 * 0.4);
		}

		double depthIncrease = 1.0 / (1 + 500.0 / depth);
		actualDetectionCurve -= depthIncrease;

		return Math.pow(10, mag) / Math.pow(10, actualDetectionCurve) * 0.4;
	}

	public static double detectionCurve_v6(double km) {
		return 7.0 / (1.0 + 800.0 / (km + 1000.0) + 100.0 / km) - 1;
	}

	public static double fixProbability(double prob) {
		return (prob) / (100 + prob);
	}

	public static double probability_v42(double mag, double dist) {
		return Math.pow(10, mag - detectionCurve_v42(dist));
	}

	public static double detectionCurve_v42(double dist) {
		return Math.log10((dist + 2.5) / 2.5) * 1.75;
	}

	public static void main(String[] args) {
		System.out.println(probability_v6(5.05, 16000, 600) * 100);
	}

}
