package zejfseis4.scale;

import java.io.IOException;

import zejfseis4.exception.FatalIOException;

public class Scales {

	public static ScaleRange MAG = new ScaleRange("scale1.png", 0, 10);

	public static ScaleRange DEPTH = new ScaleRange("scale1.png", 26, -10) {
		public java.awt.Color getColor(double value) {
			return super.getColor(Math.sqrt(Math.abs(value)));
		};
	};

	public static ScaleRange DIST = new ScaleRange("scale1.png", 141, 0) {
		public java.awt.Color getColor(double value) {
			return super.getColor(Math.sqrt(Math.abs(value)));
		};
	};

	public static ScaleRange DETECTABLE = new ScaleRange("scale1.png", 0, 3) {
		public java.awt.Color getColor(double value) {
			return super.getColor(value>1?1+Math.log10(Math.abs(value)):value);
		};
	};

	public static ScaleRange SPECTRO = new ScaleRange("scale2.png", 0, 1000);

	public static void loadScales() throws FatalIOException {
		for (Scale s : new Scale[] { MAG, DEPTH, DIST, DETECTABLE, SPECTRO }) {
			try {
				s.loadColors();
			} catch (IOException | IllegalArgumentException e) {
				throw new FatalIOException("Unable to load scale", e);
			}
		}
	}

}
