package com.morce.zejfseis4.ui.model;

import java.time.LocalDateTime;
import java.util.List;

import com.morce.zejfseis4.events.Event;

public class EventTableModel extends FilterableTableModel<Event> {

	private static final long serialVersionUID = 1L;

	private final List<Column<Event, ?>> columns = List.of(
			Column.readonly("Date", LocalDateTime.class, Event::getOriginDate),
			Column.readonly("Region", String.class, Event::getRegion),
			Column.readonly("M", String.class, Event::getMagType),
			Column.readonly("Magnitude", Double.class, Event::getMag)
			
	);

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
