package zejfseis4.ui.renderer;

import java.awt.Color;

import zejfseis4.events.DetectionStatus;
import zejfseis4.events.Event;

public class DetectedRenderer extends TableCellRendererAdapter<Event, DetectionStatus> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Color getBackground(Event entity, DetectionStatus detectionStatus) {
		switch (detectionStatus) {
		case BROKEN:
			return Color.gray;
		case NOISE:
			return Color.gray;
		case NOT_DETECTED:
			return Color.LIGHT_GRAY;
		case UNKNOWN:
			return Color.white;
		case DETECTED:
			return entity.getIntensityCategory().getColor();
		default:
			return Color.LIGHT_GRAY;
		}
	}
	
	@Override
	public String getText(Event entity, DetectionStatus value) {
		if(entity.getDetectionStatus().equals(DetectionStatus.DETECTED)) {
			return entity.getIntensityCategory().getName();
		}else {
			return super.getText(entity, value);
		}
	}

}
