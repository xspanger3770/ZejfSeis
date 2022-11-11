package com.morce.zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import com.morce.zejfseis4.data.DataManager;
import com.morce.zejfseis4.events.DetectionStatus;
import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.ui.model.EventTableModel;

public class EventsTab extends JPanel {

	private static final long serialVersionUID = 2678465200080299704L;
	private Calendar displayedTime;
	private JLabel labelTime;
	private JTable table;

	private List<Event> data = new ArrayList<>();
	private EventTableModel tableModel;

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
						} catch (Exception e) {
							e.printStackTrace();
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
		table.setAutoCreateRowSorter(true);
		table.setFont(new Font("Calibri", Font.BOLD, 16));
		table.setRowHeight(24);
		table.setGridColor(Color.black);
		table.setShowGrid(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tableModel.getColumnRenderer(i));
		}

		JComboBox<DetectionStatus> categoryJComboBox = new JComboBox<>(DetectionStatus.values());
		table.setDefaultEditor(DetectionStatus.class, new DefaultCellEditor(categoryJComboBox));

		return table;
	}

	public void updatePanel() {
		labelTime.setText(String.format("%s %s", DataManager.MONTHS[displayedTime.get(Calendar.MONTH)],
				displayedTime.get(Calendar.YEAR)));

		ArrayList<Event> events = (ArrayList<Event>) ZejfSeis4.getEventManager()
				.getEventMonth(displayedTime.get(Calendar.YEAR), displayedTime.get(Calendar.MONTH), true).getEvents();

		System.out.println(events.size());

		this.data.clear();
		this.data.addAll(events);

		tableModel.applyFilter();
	}

}
