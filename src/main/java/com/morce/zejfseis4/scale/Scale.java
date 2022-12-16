package com.morce.zejfseis4.scale;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.morce.zejfseis4.exception.FatalIOException;

public abstract class Scale {

	private String resourceName;
	private ArrayList<Color> colors;
	
	public static final Color ERROR_COLOR = Color.white;

	public Scale(String resourceName) {
		this.resourceName = resourceName;
	}

	void loadColors() throws IOException {
		colors = new ArrayList<Color>();
		BufferedImage img = getImage(getResourceName());
		Color last = null;
		for (int y = 0; y < img.getHeight(); y++) {
			Color c = new Color(img.getRGB(0, y));
			if (!(c.equals(Color.black)) && (last == null || !last.equals(c))) {
				colors.add(c);
			}
			if (!(c.equals(Color.black))) {
				last = c;
			}
		}
	}

	public String getResourceName() {
		return resourceName;
	}

	public ArrayList<Color> getColors() {
		return colors;
	}

	public abstract Color getColor(double value);

	public static BufferedImage getImage(String path) throws IOException {
		return ImageIO.read(Scale.class.getResource(path));
	}

}
