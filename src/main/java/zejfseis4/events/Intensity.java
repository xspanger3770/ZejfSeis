package zejfseis4.events;

import java.awt.Color;

import zejfseis4.main.Settings;

public enum Intensity {

	NANO("Nano", new Color(255, 255, 255)),
	MICRO("Micro", new Color(200, 230, 255)),
	TINY("Tiny",  new Color(51, 187, 255)),
	MODERATE("Moderate", new Color(50, 220, 50)),
	STRONG("Strong", Color.yellow),
	VERY_STRONG("Very Strong", new Color(255, 117, 26)),
	MAJOR_L("Major-", Color.red),
	MAJOR_U("Major+", new Color(204, 0, 0)),
	EXTREME_L("Extreme-", new Color(153, 0, 0)),
	EXTREME_U("Extreme+", new Color(102, 0, 0)),
	BLACK_SWAN("BLACK-SWAN", Color.black);

	private String name;
	private Color color;

	private Intensity(String name, Color color) {
		this.name = name;
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}
	
	public static int getIntensity(int index) {
		return (int) (Settings.NOISE_LEVEL * Math.pow(2, index));
	}

	public static Intensity get(int intensity) {
		intensity = Math.abs(intensity);
		
		if(Settings.NOISE_LEVEL <= 0) {
			return NANO;
		}
		
		int index = (int) ((Math.log(intensity / (Settings.NOISE_LEVEL / 2.0)) / Math.log(2)));
		index = Math.max(0, Math.min(Intensity.values().length - 1, index));
		
		return Intensity.values()[index];
	}
}
