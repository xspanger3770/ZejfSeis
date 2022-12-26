package com.morce.zejfseis4.ui.renderer;

import java.awt.Color;

import com.morce.zejfseis4.events.Event;

public class NameRenderer extends TableCellRendererAdapter<Event, String> {

	private static final long serialVersionUID = 1L;

	public NameRenderer(int align) {
		super(align);
	}

	@Override
	public String getText(Event entity, String value) {
		String text = super.getText();
		if (text.length() < 2) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}

	@Override
	public Color getBackground(Event entity, String value) {
		double pct = entity.calculateDetectionPct();
		if (pct > 500) {
			return Color.black;
		} else if (pct > 100) {
			return Color.red;
		} else if (pct > 20) {
			return Color.orange;
		} else if (pct > 5) {
			return Color.yellow;
		}
		return Color.white;
	}

	@Override
	public Color getForeground(Event entity, String value) {
		return ScaleRenderer.foregroundColor(getBackground(entity, value));
	}

}
