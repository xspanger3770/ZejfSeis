package com.morce.zejfseis4.ui.renderer;

import java.awt.Color;

import com.morce.zejfseis4.events.DetectionStatus;
import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.events.Intensity;

public class IntensityRenderer extends TableCellRendererAdapter<Event, Integer> {

	private static final long serialVersionUID = 1L;

	@Override
	public Color getBackground(Event entity, Integer value) {
		if (entity.getDetectionStatus().equals(DetectionStatus.DETECTED)) {
			return Intensity.get(value).getColor();
		} else {
			return Color.white;
		}
	};

	@Override
	public String getText(Event entity, Integer value) {
		if (entity.getDetectionStatus().equals(DetectionStatus.DETECTED)) {
			return String.format("%,d", value);
		} else {
			return "---";
		}
	}

}
