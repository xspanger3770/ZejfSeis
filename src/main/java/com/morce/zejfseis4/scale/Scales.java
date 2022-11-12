package com.morce.zejfseis4.scale;

public class Scales {

	public static Scale MAG = new ScaleRange("scale1.png", 0, 10);
	public static Scale DEPTH = new ScaleRange("scale1.png", 26, -10) {
		public java.awt.Color getColor(double value) {
			return super.getColor(Math.sqrt(Math.abs(value)));
		};
	};

	public static Scale DIST = new ScaleRange("scale1.png", 141, 0) {
		public java.awt.Color getColor(double value) {
			return super.getColor(Math.sqrt(Math.abs(value)));
		};
	};

	public static Scale DETECTABLE = new ScaleRange("scale1.png", 0, 3) {
		public java.awt.Color getColor(double value) {
			return super.getColor(Math.sqrt(Math.abs(value)));
		};
	};

}
