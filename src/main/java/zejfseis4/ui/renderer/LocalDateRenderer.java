package zejfseis4.ui.renderer;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import zejfseis4.events.Event;

public class LocalDateRenderer extends TableCellRendererAdapter<Event, LocalDateTime> {

	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	@Override
	public String getText(Event entity, LocalDateTime value) {
		return formatter.format(value);
	}

	@Override
	public Color getBackground(Event entity, LocalDateTime value) {
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
	public Color getForeground(Event entity, LocalDateTime value) {
		return ScaleRenderer.foregroundColor(getBackground(entity, value));
	}

}
