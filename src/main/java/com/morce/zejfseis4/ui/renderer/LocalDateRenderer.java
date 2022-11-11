package com.morce.zejfseis4.ui.renderer;

import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class LocalDateRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public LocalDateRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof LocalDateTime) {
			setText(formatter.format((LocalDateTime) value));
		}
		return this;
	}

}
