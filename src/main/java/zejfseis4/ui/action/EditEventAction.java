package zejfseis4.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

import zejfseis4.events.Event;
import zejfseis4.ui.EventExplorer;
import zejfseis4.ui.model.EventTableModel;

public class EditEventAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTable eventsTable;

	public EditEventAction(JTable eventsTable) {
		super("Edit");
		this.eventsTable = eventsTable;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int[] selectedRows = eventsTable.getSelectedRows();
        if (selectedRows.length != 1) {
            throw new IllegalStateException("Invalid selected rows count (must be 1): " + selectedRows.length);
        }
        if (eventsTable.isEditing()) {
        	eventsTable.getCellEditor().cancelCellEditing();
        }
        var model = (EventTableModel) eventsTable.getModel();
        int modelRow = eventsTable.convertRowIndexToModel(selectedRows[0]);
        Event event = model.getEntity(modelRow);
        
        new EventExplorer(event);
	}

}
