package com.morce.zejfseis4.ui.renderer;

import com.morce.zejfseis4.events.Event;

public class NameRenderer extends TableCellRendererAdapter<Event, String> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getText(Event entity, String value) {
		String text = super.getText();
		if(text.length() < 2) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}

}
