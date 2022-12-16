package com.morce.zejfseis4.scale;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.morce.zejfseis4.exception.FatalIOException;

public class Scales {

	public static ScaleRange MAG;
	public static ScaleRange DIST;
	public static ScaleRange DETECTABLE;
	public static ScaleRange DEPTH;
	public static Color[] scale;

	public static void loadScales() throws FatalIOException {
		MAG = new ScaleRange("scale1.png", 0, 10);
		DEPTH = new ScaleRange("scale1.png", 26, -10) {
			public java.awt.Color getColor(double value) {
				return super.getColor(Math.sqrt(Math.abs(value)));
			};
		};

		DIST = new ScaleRange("scale1.png", 141, 0) {
			public java.awt.Color getColor(double value) {
				return super.getColor(Math.sqrt(Math.abs(value)));
			};
		};

		DETECTABLE = new ScaleRange("scale1.png", 0, 3) {
			public java.awt.Color getColor(double value) {
				return super.getColor(Math.sqrt(Math.abs(value)));
			};
		};
		
		BufferedImage img;
		try {
			img = ImageIO.read(Scales.class.getResource("scale2.png"));
		} catch (IOException | IllegalArgumentException e) {
			throw new FatalIOException("Unable to load scale", e);
		}
		scale = new Color[img.getHeight()];
		for (int i = 0; i < img.getHeight(); i++) {
			scale[i] = new Color(img.getRGB(0, i));
		}
	}

}
