package zejfseis4.ui.renderer;

import java.awt.Color;

import zejfseis4.events.DetectionStatus;
import zejfseis4.events.Event;

public class DetectedRenderer extends TableCellRendererAdapter<Event, DetectionStatus> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Color getBackground(Event entity, DetectionStatus detectionStatus) {
        return switch (detectionStatus) {
            case BROKEN -> Color.gray;
            case NOISE -> Color.gray;
            case NOT_DETECTED -> Color.LIGHT_GRAY;
            case UNKNOWN -> Color.white;
            case DETECTED -> entity.getIntensityCategory().getColor();
            default -> Color.LIGHT_GRAY;
        };
	}
	
	@Override
	public Color getForeground(Event entity, DetectionStatus value) {
		return ScaleRenderer.foregroundColor(getBackground(entity, value));
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
