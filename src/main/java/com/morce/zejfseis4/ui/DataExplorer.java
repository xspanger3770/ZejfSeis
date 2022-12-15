package com.morce.zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.morce.zejfseis4.events.ManualEvent;
import com.morce.zejfseis4.exception.EventsIOException;
import com.morce.zejfseis4.main.ZejfSeis4;

public class DataExplorer extends JFrame {

	private static final long serialVersionUID = 1L;
	private DataExplorerPanel dataExplorerPanel;
	private JButton buttonAddEvent;

	public DataExplorer(long start, long end) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dataExplorerPanel.close();
			}
		});

		dataExplorerPanel = new DataExplorerPanel(start, end) {
			private static final long serialVersionUID = 1L;

			@Override
			public void wavesSelected() {
				boolean wavesSelected = dataExplorerPanel.pWaveTime > 0 && dataExplorerPanel.sWaveTime > 0;
				buttonAddEvent.setEnabled(wavesSelected);
			}
		};
		dataExplorerPanel.setPreferredSize(new Dimension(600, 400));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(dataExplorerPanel, BorderLayout.CENTER);

		dataExplorerPanel.enableSelecting = true;

		JPanel controlPanel = new JPanel();

		buttonAddEvent = new JButton("Add Event");
		buttonAddEvent.setEnabled(false);

		buttonAddEvent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean wavesSelected = dataExplorerPanel.pWaveTime > 0 && dataExplorerPanel.sWaveTime > 0;
				if (wavesSelected) {
					ManualEvent manualEvent = new ManualEvent(dataExplorerPanel.pWaveTime, dataExplorerPanel.sWaveTime);
					try {
						ZejfSeis4.getEventManager().newEvent(manualEvent);
					} catch (EventsIOException e1) {
						ZejfSeis4.errorDialog(e1);
					}
					ZejfSeis4.getFrame().getEventsTab().updatePanel();
					new EventExplorer(manualEvent);
					DataExplorer.this.dispose();
				}
			}
		});

		controlPanel.add(buttonAddEvent);
		getContentPane().add(controlPanel, BorderLayout.NORTH);

		pack();
		setTitle("Data Explorer");
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
