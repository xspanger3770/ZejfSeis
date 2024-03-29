package zejfseis4.ui.renderer;

import java.awt.Color;

import zejfseis4.events.Event;
import zejfseis4.scale.Scale;

public class ScaleRenderer extends TableCellRendererAdapter<Event, Double> {

	private static final long serialVersionUID = 1L;
	private final Scale scale;
	private String format;

	public ScaleRenderer(Scale scale, String format) {
		super();
		this.scale = scale;
		this.format = format;
	}

	@Override
	public Color getBackground(Event entity, Double value) {
		Color col = scale.getColor(value);
		return col;
	}

	@Override
	public Color getForeground(Event entity, Double value) {
		Color col = scale.getColor(value);
		return foregroundColor(col);
	}
	
	public static Color foregroundColor(Color col) {
		return (col.getRed() * 0.299 + col.getGreen() * 0.587 + col.getBlue() * 0.05 > 60 ? Color.black : Color.white);
	}

	@Override
	public String getText(Event entity, Double value) {
		return String.format(format, value);
	}

}
