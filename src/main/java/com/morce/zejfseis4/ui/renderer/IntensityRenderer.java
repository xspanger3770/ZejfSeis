package com.morce.zejfseis4.ui.renderer;

import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.events.Intensity;

public class IntensityRenderer extends TableCellRendererAdapter<Event, Integer> {

	private static final long serialVersionUID = 1L;

	public java.awt.Color getBackground(Event entity, Integer value) {
		return Intensity.get(value).getColor();
	};
	
	@Override
	public String getText(Event entity, Integer value) {
		return String.format("%,d", value);
	}

}
