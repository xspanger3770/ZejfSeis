package com.morce.zejfseis4.ui.renderer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.morce.zejfseis4.events.Event;

public class LocalDateRenderer extends TableCellRendererAdapter<Event, LocalDateTime> {

	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	@Override
	public String getText(Event entity, LocalDateTime value) {
		return formatter.format(value);
	}

}
