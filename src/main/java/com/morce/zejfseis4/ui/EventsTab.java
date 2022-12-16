package com.morce.zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.tinylog.Logger;

import com.morce.zejfseis4.data.DataManager;
import com.morce.zejfseis4.events.DetectionStatus;
import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.exception.FatalIOException;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.ui.action.EditEventAction;
import com.morce.zejfseis4.ui.model.EventTableModel;

public class EventsTab extends JPanel {

	private static final long serialVersionUID = 2678465200080299704L;
	private Calendar displayedTime;
	private JLabel labelTime;
	private JTable table;

	private List<Event> data = new ArrayList<>();
	private EventTableModel tableModel;
	private EditEventAction editEventAction;

	public EventsTab() {
		setLayout(new BorderLayout(0, 0));

		displayedTime = Calendar.getInstance();
		displayedTime.setTimeInMillis(System.currentTimeMillis());
		displayedTime.set(Calendar.DATE, 1);

		JPanel panelControl = new JPanel();
		panelControl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panelControl, BorderLayout.NORTH);

		JButton btnBackM = new JButton("<<");
		panelControl.add(btnBackM);
		btnBackM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayedTime.add(Calendar.YEAR, -1);
				updatePanel();
			}
		});
		JButton btnBackS = new JButton("<");
		panelControl.add(btnBackS);
		btnBackS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayedTime.add(Calendar.MONTH, -1);
				updatePanel();
			}
		});
		labelTime = new JLabel("...", SwingConstants.CENTER);
		labelTime.setFont(new Font("Calibri", Font.BOLD, 22));
		labelTime.setPreferredSize(new Dimension(240, 26));

		labelTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					displayedTime.setTimeInMillis(System.currentTimeMillis());
					updatePanel();
				}
			}
		});

		panelControl.add(labelTime);

		JButton btnForwardS = new JButton(">");
		panelControl.add(btnForwardS);
		btnForwardS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayedTime.add(Calendar.MONTH, +1);
				if (displayedTime.getTimeInMillis() > System.currentTimeMillis()) {
					displayedTime.setTimeInMillis(System.currentTimeMillis());
					displayedTime.set(Calendar.DATE, 1);
				}
				updatePanel();
			}
		});
		JButton btnForwardM = new JButton(">>");
		btnForwardM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayedTime.add(Calendar.YEAR, +1);
				if (displayedTime.getTimeInMillis() > System.currentTimeMillis()) {
					displayedTime.setTimeInMillis(System.currentTimeMillis());
					displayedTime.set(Calendar.DATE, 1);
				}
				updatePanel();
			}
		});
		panelControl.add(btnForwardM);

		JButton download = new JButton("Download");
		download.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				download.setEnabled(false);
				download.setText("Downloading...");
				Thread downloader = new Thread("Whole month download") {
					public void run() {
						try {
							ZejfSeis4.getEventManager().getFdsnDownloader().downloadWholeMonth(displayedTime);
						} catch (FatalIOException e) {
							ZejfSeis4.handleException(e);
						} catch (IOException e) {
							Logger.error(e);
						}
						download.setEnabled(true);
						download.setText("Download");
					};
				};

				downloader.start();
			}
		});
		panelControl.add(download);

		add(new JScrollPane(table = createTable()), BorderLayout.CENTER);

		updatePanel();
	}

	private JTable createTable() {
		tableModel = new EventTableModel(data) {
			private static final long serialVersionUID = 1L;

			@Override
			public void dataUpdated() {
				// TODO
			}

			@Override
			public boolean accept(Event transaction) {
				return true;
			}
		};

		JTable table = new JTable(tableModel);
		table.setFont(new Font("Calibri", Font.BOLD, 16));
		table.setRowHeight(24);
		table.setGridColor(Color.black);
		table.setShowGrid(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoCreateRowSorter(true);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		int columnIndexToSort = 0;
		sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.DESCENDING));

		sorter.setSortKeys(sortKeys);
		sorter.sort();

		editEventAction = new EditEventAction(table);

		table.getSelectionModel().addListSelectionListener(this::rowSelectionChanged);

		table.setComponentPopupMenu(createPopupMenu());

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable table = (JTable) mouseEvent.getSource();
				if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 2
						&& table.getSelectedRow() != -1) {
					editEventAction.actionPerformed(null);
				}
			}
		});

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tableModel.getColumnRenderer(i));
		}

		JComboBox<DetectionStatus> categoryJComboBox = new JComboBox<>(DetectionStatus.values());
		table.setDefaultEditor(DetectionStatus.class, new DefaultCellEditor(categoryJComboBox));

		return table;
	}

	private void rowSelectionChanged(ListSelectionEvent event) {
		var selectionModel = (ListSelectionModel) event.getSource();
		var count = selectionModel.getSelectedItemsCount();
		editEventAction.setEnabled(count == 1);
	}

	private JPopupMenu createPopupMenu() {
		var menu = new JPopupMenu();
		menu.add(editEventAction);
		return menu;
	}

	public void updatePanel() {
		labelTime.setText(String.format("%s %s", DataManager.MONTHS[displayedTime.get(Calendar.MONTH)],
				displayedTime.get(Calendar.YEAR)));

		ArrayList<Event> events = new ArrayList<>();
		try {
			events = (ArrayList<Event>) ZejfSeis4.getEventManager()
					.getEventMonth(displayedTime.get(Calendar.YEAR), displayedTime.get(Calendar.MONTH), true)
					.getEvents();
		} catch (FatalIOException e) {
			ZejfSeis4.handleException(e);
		}

		this.data.clear();
		this.data.addAll(events);

		tableModel.applyFilter();
	}

	public JTable getTable() {
		return table;
	}

}
