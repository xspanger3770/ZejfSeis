package com.morce.zejfseis4.ui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.morce.zejfseis4.ui.model.FilterableTableModel;

public class TableCellRendererAdapter<E, T> extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public TableCellRendererAdapter() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value != null) {
			try {
				if (table.getModel() instanceof FilterableTableModel<?>) {
					E entity = (E) ((FilterableTableModel<?>) table.getModel())
							.getEntity(table.getRowSorter().convertRowIndexToModel(row));
					T t = (T) value;
					Color bck = getBackground(entity, t);
					if(bck != null) {
						setBackground(bck);
					}
					setForeground(getForeground(entity, t));
					setText(getText(entity, t));
				}

			} catch (ClassCastException e) {

			}
		}

		return this;
	}

	public String getText(E entity, T value) {
		return getText();
	}

	public Color getForeground(E entity, T value) {
		return getForeground();
	}

	public Color getBackground(E entity, T value) {
		return null;
	}

}
