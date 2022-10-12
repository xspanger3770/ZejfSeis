package com.morce.zejfseis4.data;

import java.awt.Color;

public enum Intensity {

	MICRO("Micro", 80_000, new Color(204, 238, 255)),
	TINY("Tiny", 240_000,  new Color(51, 187, 255)),
	MODERATE("Moderate", 720_000, new Color(51, 204, 51)),
	STRONG("Strong", 2_160_000, Color.yellow),
	VERY_STRONG("Very Strong", 6_480_000, new Color(255, 117, 26)),
	MAJOR_L("Major-", 19_440_000, Color.red),
	MAJOR_U("Major+", 58_320_000, new Color(204, 0, 0)),
	EXTREME_L("Extreme-", 174_960_000, new Color(153, 0, 0)),
	EXTREME_U("Extreme+", 524_880_000, new Color(102, 0, 0)),
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
