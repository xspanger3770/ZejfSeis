package com.morce.zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import com.morce.zejfseis4.data.DataManager;
import com.morce.zejfseis4.events.Event;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.scale.Scales;
import com.morce.zejfseis4.ui.model.EventTableModel;
import com.morce.zejfseis4.ui.renderer.LocalDateRenderer;
import com.morce.zejfseis4.ui.renderer.ScaleRenderer;

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

		table.setDefaultRenderer(LocalDateTime.class, new LocalDateRenderer());

		table.getColumnModel().getColumn(1).setCellRenderer(new ScaleRenderer(Scales.SYS));
		// table.getColumnModel().getColumn(2).setCellRenderer(new
		// ScaleRenderer(Scales.DIA));
		// table.getColumnModel().getColumn(3).setCellRenderer(new
		// ScaleRenderer(Scales.HR));

		// pressureTable.getSelectionModel().addListSelectionListener(this::rowSelectionChanged);

		return table;
	}

	public void updatePanel() {
		System.out.println("UPDATEEE");

		labelTime.setText(String.format("%s %s", DataManager.MONTHS[displayedTime.get(Calendar.MONTH)],
				displayedTime.get(Calendar.YEAR)));

		@SuppressWarnings("unchecked")
		ArrayList<Event> events = (ArrayList<Event>) ZejfSeis4.getEventManager()
				.getEventMonth(displayedTime.get(Calendar.YEAR), displayedTime.get(Calendar.MONTH), true).getEvents();
		
		System.out.println(events.size());
		
		this.data.clear();
		this.data.addAll(events);

		tableModel.applyFilter();
	}

}
