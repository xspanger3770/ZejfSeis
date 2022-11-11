package com.morce.zejfseis4.ui.model;

import java.time.LocalDateTime;
import java.util.List;

import com.morce.zejfseis4.events.DetectionStatus;
import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.events.EventDistance;
import com.morce.zejfseis4.scale.Scales;
import com.morce.zejfseis4.ui.renderer.DetectableRenderer;
import com.morce.zejfseis4.ui.renderer.DetectedRenderer;
import com.morce.zejfseis4.ui.renderer.EventDistanceRenderer;
import com.morce.zejfseis4.ui.renderer.IntensityRenderer;
import com.morce.zejfseis4.ui.renderer.LocalDateRenderer;
import com.morce.zejfseis4.ui.renderer.MagnitudeRenderer;
import com.morce.zejfseis4.ui.renderer.NameRenderer;
import com.morce.zejfseis4.ui.renderer.ScaleRenderer;
import com.morce.zejfseis4.ui.renderer.TableCellRendererAdapter;

public class EventTableModel extends FilterableTableModel<Event> {

	private static final long serialVersionUID = 1L;

	private final List<Column<Event, ?>> columns = List.of(
			Column.readonly("Date", LocalDateTime.class, Event::getOriginDate, new LocalDateRenderer()),
			Column.readonly("Region", String.class, Event::getRegion, new NameRenderer()),
			Column.readonly("Type", EventDistance.class, Event::getEventDistance, new EventDistanceRenderer()),
			Column.readonly("Magnitude", Double.class, Event::getMag, new MagnitudeRenderer()),
			Column.readonly("Depth", Double.class, Event::getDepth, new ScaleRenderer(Scales.DEPTH, "%.1f km")),
			Column.readonly("Distance", Double.class, Event::getDistance, new ScaleRenderer(Scales.DIST, "%,.1f km")),
			Column.readonly("Detectable", Double.class, Event::calculateDetectionPct, new DetectableRenderer()),
			Column.editable("Detected", DetectionStatus.class, Event::getDetectionStatus, Event::setDetectionStatus,
					new DetectedRenderer()),
			Column.editable("Intensity", Integer.class, Event::getIntensity, Event::setIntensity,
					new IntensityRenderer()));

	public EventTableModel(List<Event> data) {
		super(data);
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex).getName();
	}

	public TableCellRendererAdapter<?, ?> getColumnRenderer(int columnIndex) {
		return columns.get(columnIndex).getRenderer();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns.get(columnIndex).getColumnType();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columns.get(columnIndex).isEditable();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Event event = getEntity(rowIndex);
		return columns.get(columnIndex).getValue(event);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		Event event = getEntity(rowIndex);
		columns.get(columnIndex).setValue(value, event);
	}

}
