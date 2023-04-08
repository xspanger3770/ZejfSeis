package zejfseis4.ui.renderer;

import java.awt.Color;

import zejfseis4.events.Event;
import zejfseis4.events.EventDistance;

public class EventDistanceRenderer extends TableCellRendererAdapter<Event, EventDistance> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Color getBackground(Event entity, EventDistance value) {
		return value.getColor();
	}
	
	@Override
	public String getText(Event entity, EventDistance value) {
		return value.getName();
	}

}
