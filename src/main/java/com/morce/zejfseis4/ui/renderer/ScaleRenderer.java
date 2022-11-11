package com.morce.zejfseis4.ui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.morce.zejfseis4.scale.Scale;

public class ScaleRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private final Scale scale;

	public ScaleRenderer(Scale scale) {
		this.scale = scale;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null) {
			setBackground(Color.white);
			setForeground(Color.black);
			return this;
		}
		if (value instanceof Integer) {
			int val = (int) value;
			setText(String.format("%d", val));

			Color col = scale.getColor(val);
			setBackground(col);
			setForeground(col.getRed() * 0.299 + col.getGreen() * 0.587 + col.getBlue() * 0.114 > 60 ? Color.black
					: Color.white);
		}
		return this;
	}

}
