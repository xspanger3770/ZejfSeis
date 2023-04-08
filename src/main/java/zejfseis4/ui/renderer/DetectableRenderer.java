package zejfseis4.ui.renderer;

import zejfseis4.events.Event;
import zejfseis4.scale.Scales;

public class DetectableRenderer extends ScaleRenderer {

	private static final long serialVersionUID = 1L;

	public DetectableRenderer() {
		super(Scales.DETECTABLE, "%,.2f");
	}
	
	@Override
	public String getText(Event entity, Double value) {
		return super.getText(entity, value);
	}
	

}
