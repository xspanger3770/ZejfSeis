package zejfseis4.ui.renderer;

import zejfseis4.events.Event;
import zejfseis4.scale.Scales;

public class MagnitudeRenderer extends ScaleRenderer {

	public MagnitudeRenderer() {
		super(Scales.MAG, "%.1f");
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getText(Event entity, Double value) {
		return String.format("%s %s", super.getText(entity, value), entity.getMagType());
	}

}
