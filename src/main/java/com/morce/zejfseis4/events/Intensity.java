package com.morce.zejfseis4.events;

import java.awt.Color;

public enum Intensity {

	NANO("Nano", 80_000, new Color(255, 255, 255)),
	MICRO("Micro", 400_000, new Color(200, 230, 255)),
	TINY("Tiny", 800_000,  new Color(51, 187, 255)),
	MODERATE("Moderate", 1_600_000, new Color(50, 220, 50)),
	STRONG("Strong", 3_200_000, Color.yellow),
	VERY_STRONG("Very Strong", 6_400_000, new Color(255, 117, 26)),
	MAJOR_L("Major-", 12_800_000, Color.red),
	MAJOR_U("Major+", 25_600_000, new Color(204, 0, 0)),
	EXTREME_L("Extreme-", 51_100_000, new Color(153, 0, 0)),
	EXTREME_U("Extreme+", 102_200_000, new Color(102, 0, 0)),
	BLACK_SWAN("BLACK-SWAN", 0, Color.black);

	private String name;
	private int intensity;
	private Color color;

	private Intensity(String name, int intensity, Color color) {
		this.name = name;
		this.intensity = intensity;
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public int getIntensity() {
		return intensity;
	}

	public static Intensity get(long intensity) {
		intensity = Math.abs(intensity);
		for (int i = 0; i < Intensity.values().length; i++) {
			if (intensity < Intensity.values()[i].getIntensity()) {
				return Intensity.values()[i];
			}
		}
		return BLACK_SWAN;
	}
}
