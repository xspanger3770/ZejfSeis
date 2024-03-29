package zejfseis4.ui.renderer;

import java.awt.Color;

import zejfseis4.events.DetectionStatus;
import zejfseis4.events.Event;
import zejfseis4.events.Intensity;

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
	public Color getForeground(Event entity, Integer value) {
		return ScaleRenderer.foregroundColor(getBackground(entity, value));
	}
	

	@Override
	public String getText(Event entity, Integer value) {
		if (entity.getDetectionStatus().equals(DetectionStatus.DETECTED)) {
			return String.format("%,d", value);
		} else {
			return "---";
		}
	}

}
