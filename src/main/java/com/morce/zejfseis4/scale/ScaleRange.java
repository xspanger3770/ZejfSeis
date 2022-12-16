package com.morce.zejfseis4.scale;

import java.awt.Color;

import com.morce.zejfseis4.exception.FatalIOException;

public class ScaleRange extends Scale {

	private double max;
	private double min;

	public ScaleRange(String resourceName, double min, double max) throws FatalIOException {
		super(resourceName);
		this.max = max;
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	@Override
	public Color getColor(double value) {
		int index = (int) Math.round((value - min) / (max - min) * (getColors().size() - 1));
		index = Math.max(0, Math.min(getColors().size() - 1, index));
		return getColors().get(index);
	}

}
