package zejfseis4.events;

import java.awt.Color;

public enum EventDistance {

	UNKNOWN("Unknown",Color.WHITE),
	NEARBY("Nearby", new Color(230,0,0)),
	LOCAL("Local", new Color(54,204,255)),
	REGIONAL("Regional", Color.YELLOW),
	TELESEISMIC("Teleseismic", new Color(255,153,51)),
	SHADOW("Shadow Zone", new Color(102,102,153)),
	CORE("Core", new Color(0,180,0));

	private String name;
	private Color color;

	private EventDistance(String name, Color color) {
		this.name = name;
		this.color=color;
	}

	public String getName() {
		return name;
	}
	
	public Color getColor() {
		return color;
	}

	public static EventDistance get(double distance) {
		if (distance < 0) {
			return UNKNOWN;
		} else if (distance <= 50) {
			return NEARBY;
		} else if (distance <= 200) {
			return LOCAL;
		} else if (distance <= 3000) {
			return REGIONAL;
		} else if (distance <= 11000) {
			return TELESEISMIC;
		} else if (distance <= 15000) {
			return SHADOW;
		} else {
			return CORE;
		}
	}

}
