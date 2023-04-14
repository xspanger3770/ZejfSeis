package zejfseis4.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import zejfseis4.exception.FatalIOException;

public class Settings {

	private static File propertiesFile = new File(ZejfSeis4.MAIN_FOLDER, "properties.properties");
	public static double SPECTRO_GAIN;
	public static int REALTIME_DURATION_SECONDS;
	public static int WINDOW;
	public static double MAX_FREQUENCY;
	public static double MIN_FREQUENCY;
	public static int DRUM_SPACE_INDEX;
	public static double DRUM_GAIN;
	public static boolean ANTIALIAS;
	public static int DECIMATE;
	public static String ADDRESS;
	public static int PORT;
	public static double SPECTRO_MAX_FREQUENCY;
	
	public static double LOCATION_LATITUDE;
	public static double LOCATION_LONGITUDE;
	
	public static int NOISE_LEVEL;
	
	public static final int[] DURATIONS = { 5, 10, 15, 20, 30, 60, 60 * 2, 60 * 3, 60 * 5, 60 * 10 };
	public static final String[] DURATIONS_NAMES = { "5 Seconds", "10 Seconds", "15 Seconds", "20 Seconds",
			"30 Seconds", "1 Minute", "2 Minutes", "3 Minutes", "5 Minutes", "10 Minutes" };

	public static final int[] DRUM_SPACES = { 1, 2, 5, 10, 15, 20, 30, 60 };
	public static int SAMPLE_RATES[] = { 20, 40, 60, 100, 200 };
	
	public static int SERIAL_PORT_SAMPLE_RATE;

	static void loadProperties() throws FatalIOException {
		Properties prop = new Properties();
		try {
			if (!propertiesFile.exists()) {
				propertiesFile.createNewFile();
			}
			prop.load(new FileInputStream(propertiesFile));
		} catch (IOException e) {
			throw new FatalIOException("Failed to load properties", e);
		}
		MAX_FREQUENCY = Double.valueOf(prop.getProperty("max_frequency", "20.0"));
		MIN_FREQUENCY = Double.valueOf(prop.getProperty("min_frequency", "0.0"));

		SPECTRO_GAIN = Double.valueOf(prop.getProperty("spectro_gain", "0.25"));
		REALTIME_DURATION_SECONDS = DURATIONS[Integer.valueOf(prop.getProperty("realtime_duration_index", "8"))];
		WINDOW = Integer.valueOf(prop.getProperty("window", "160"));
		DRUM_SPACE_INDEX = Integer.valueOf(prop.getProperty("drum_space_index", "3"));
		DRUM_GAIN = Double.valueOf(prop.getProperty("drum_gain", "1.0"));
		ANTIALIAS = Boolean.valueOf(prop.getProperty("antialiasing", "false"));
		DECIMATE = Integer.valueOf(prop.getProperty("decimate", "1"));
		ADDRESS = String.valueOf(prop.getProperty("address", "0.0.0.0"));
		PORT = Integer.valueOf(prop.getProperty("port", "6222"));
		SPECTRO_MAX_FREQUENCY = Double.valueOf(prop.getProperty("spectro_max_freq", "20.0"));
		
		LOCATION_LATITUDE = Double.valueOf(prop.getProperty("geo_lat", "0.0"));
		LOCATION_LONGITUDE = Double.valueOf(prop.getProperty("geo_lon", "0.0"));
		
		NOISE_LEVEL = Integer.valueOf(prop.getProperty("noise", "0"));
		
		SERIAL_PORT_SAMPLE_RATE = Integer.valueOf(prop.getProperty("serial_sample_rate", "40"));
		
		saveProperties();
	}

	public static void saveProperties() throws FatalIOException {
		Properties prop = new Properties();
		prop.setProperty("max_frequency", MAX_FREQUENCY + "");
		prop.setProperty("min_frequency", MIN_FREQUENCY + "");
		prop.setProperty("spectro_gain", SPECTRO_GAIN + "");
		prop.setProperty("realtime_duration_index", (Arrays.binarySearch(DURATIONS, REALTIME_DURATION_SECONDS) + ""));
		prop.setProperty("window", WINDOW + "");
		prop.setProperty("drum_space_index", DRUM_SPACE_INDEX + "");
		prop.setProperty("drum_gain", DRUM_GAIN + "");
		prop.setProperty("antialiasing", ANTIALIAS + "");
		prop.setProperty("decimate", DECIMATE + "");
		prop.setProperty("address", ADDRESS + "");
		prop.setProperty("port", PORT + "");
		prop.setProperty("spectro_max_freq", SPECTRO_MAX_FREQUENCY + "");
		prop.setProperty("geo_lat", LOCATION_LATITUDE + "");
		prop.setProperty("geo_lon", LOCATION_LONGITUDE + "");
		prop.setProperty("noise", NOISE_LEVEL + "");
		prop.setProperty("serial_sample_rate", SERIAL_PORT_SAMPLE_RATE + "");
		try {
			prop.store(new FileOutputStream(propertiesFile), "");
		} catch (IOException e) {
			throw new FatalIOException("Failed to store properties", e);
		}
	}

}
