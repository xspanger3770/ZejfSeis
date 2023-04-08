package zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import zejfseis4.events.CatalogueEvent;
import zejfseis4.events.DetectionStatus;
import zejfseis4.events.Event;
import zejfseis4.events.ManualEvent;
import zejfseis4.exception.FatalApplicationException;
import zejfseis4.exception.FatalIOException;
import zejfseis4.main.ZejfSeis4;
import zejfseis4.utils.TravelTimeTable;

public class EventExplorer extends JFrame {

	private static final long serialVersionUID = 1L;
	private DataExplorerPanel dataExplorerPanel;
	private Event event;
	private JPanel eventPanel;
	private JTextField textFieldIntensity;
	private boolean manual;

	// TODO manual events depth 10km but created using 0km

	public EventExplorer(Event event) {
		this.event = event;
		this.manual = event instanceof ManualEvent;
		double dist = event.getDistance();
		long pWave = manual ? ((ManualEvent) event).getpWave()
				: event.getOrigin()
						+ (long) TravelTimeTable.getPWaveTravelTime(event.getDepth(), TravelTimeTable.toAngle(dist))
								* 1000;
		long sWave = manual ? ((ManualEvent) event).getsWave()
				: event.getOrigin()
						+ (long) TravelTimeTable.getSWaveTravelTime(event.getDepth(), TravelTimeTable.toAngle(dist))
								* 1000;
		long sfcTravel = (long) (TravelTimeTable.surfaceWaveTravel(TravelTimeTable.toAngle(event.getDistance()))
				* 1000);
		long sfcStart = (long) (event.getOrigin() + sfcTravel * 1.0);
		long sfcEnd = (long) (event.getOrigin() + sfcTravel * 2.0);
		long start = pWave - 1000 * 15;
		long end = sfcEnd;
		long pkpWave = 0;
		if (dist >= 15000) {
			pkpWave = event.getOrigin()
					+ (long) TravelTimeTable.getPKPWaveTravelTime(event.getDepth(), TravelTimeTable.toAngle(dist))
							* 1000;
			start = pkpWave - 1000 * 60 * 1;
		}
		start -= (end - start) * 0.3;
		end += (end - start) * 0.25;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowClosed(e);
			}
			@Override
			public void windowClosed(WindowEvent e) {
				dataExplorerPanel.close();
			}
		});
		getContentPane().setLayout(new BorderLayout(0, 0));

		eventPanel = new JPanel();
		eventPanel.setPreferredSize(new Dimension(600, 160));
		getContentPane().add(eventPanel, BorderLayout.NORTH);

		buildEventPanel();

		dataExplorerPanel = new DataExplorerPanel(start, end);
		dataExplorerPanel.setPreferredSize(new Dimension(600, 250));
		getContentPane().add(dataExplorerPanel, BorderLayout.CENTER);

		dataExplorerPanel.enableSelecting = false;

		if (dist >= 15000) {
			dataExplorerPanel.pkpWaveTime = pkpWave;
		} else {
			dataExplorerPanel.pWaveTime = pWave;
			dataExplorerPanel.sWaveTime = sWave;
		}
		dataExplorerPanel.sfcStart = sfcStart;
		dataExplorerPanel.sfcEnd = sfcEnd;

		pack();

		setTitle("Event Explorer");
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private static SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	private JTextField textFieldMag;
	private static DecimalFormat f1d = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));

	private void buildEventPanel() {
		eventPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel(null);
		eventPanel.add(panel, BorderLayout.CENTER);

		JLabel lblNewLabel = new JLabel("ev_" + event.getID());
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		lblNewLabel.setBounds(10, 2, 264, 35);
		panel.add(lblNewLabel);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(247, 12, 179, 111);
		panel.add(panel_1);
		panel_1.setLayout(null);

		JComboBox<String> comboBoxDetectionStatus = new JComboBox<String>();
		for (DetectionStatus det : DetectionStatus.values()) {
			comboBoxDetectionStatus.addItem(det.toString()); // TODO
		}
		comboBoxDetectionStatus.setSelectedIndex(event.getDetectionStatus().ordinal());
		comboBoxDetectionStatus.setBounds(12, 14, 130, 24);
		panel_1.add(comboBoxDetectionStatus);

		textFieldIntensity = new JTextField(
				comboBoxDetectionStatus.getSelectedIndex() > 1 ? event.getIntensity() + "" : "");
		textFieldIntensity.setBounds(12, 46, 126, 26);
		panel_1.add(textFieldIntensity);
		// textFieldIntensity.setFont(checkBoxDetected.getFont());
		textFieldIntensity.setEnabled(comboBoxDetectionStatus.getSelectedIndex() > 1);
		textFieldIntensity.setColumns(10);

		JButton btnSelectIntensity = new JButton("Select");
		btnSelectIntensity.setBounds(12, 80, 126, 25);
		btnSelectIntensity.setEnabled(comboBoxDetectionStatus.getSelectedIndex() > 1);
		panel_1.add(btnSelectIntensity);

		btnSelectIntensity.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textFieldIntensity.setText("" + findPeak(dataExplorerPanel.start, dataExplorerPanel.end));
			}
		});

		comboBoxDetectionStatus.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean bool = comboBoxDetectionStatus.getSelectedIndex() > 1;
				textFieldIntensity.setEnabled(bool);
				btnSelectIntensity.setEnabled(bool);
				if (bool) {
					textFieldIntensity.setText("" + findPeak(dataExplorerPanel.start, dataExplorerPanel.end));
				}

			}
		});
		JLabel lblNewLabel_1 = new JLabel("Origin: " + dateFormat1.format(new Date(event.getOrigin())));
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNewLabel_1.setBounds(10, 33, 264, 15);
		panel.add(lblNewLabel_1);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(438, 12, 150, 111);
		panel.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblMagnitude = new JLabel("Magnitude");
		lblMagnitude.setBounds(12, 12, 96, 15);
		panel_2.add(lblMagnitude);

		textFieldMag = new JTextField();
		textFieldMag.setBounds(12, 46, 126, 26);
		textFieldMag.setEnabled(manual);
		panel_2.add(textFieldMag);
		textFieldMag.setFont(lblMagnitude.getFont());
		if (event.getMag() != ManualEvent.NO_MAG) {
			textFieldMag.setText(f1d.format(event.getMag()) + "");
		}
		textFieldMag.setColumns(10);

		JButton btnCalculateMag = new JButton("Calculate");
		btnCalculateMag.setBounds(12, 80, 126, 25);
		btnCalculateMag.setEnabled(manual);
		panel_2.add(btnCalculateMag);

		btnCalculateMag.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(event.getDistance() + ", " + Double.valueOf(textFieldIntensity.getText()));
				textFieldMag.setText(f1d.format(ManualEvent.calculateManualMagnitude(event.getDistance(),
						Double.valueOf(textFieldIntensity.getText()))));
			}
		});

		JLabel lblDepthkm = new JLabel("Depth: " + (int) (event.getDepth()) + "km");
		lblDepthkm.setBounds(10, 69, 262, 15);
		panel.add(lblDepthkm);

		JLabel lblNewLabel_2 = new JLabel("Distance: " + (int) (event.getDistance()) + "km");
		lblNewLabel_2.setBounds(10, 87, 264, 15);
		panel.add(lblNewLabel_2);

		JLabel lblRegion = new JLabel(
				event instanceof ManualEvent ? "Unknown Region" : ((CatalogueEvent) event).getRegion());
		lblRegion.setBounds(10, 51, 262, 15);
		panel.add(lblRegion);

		JPanel buttons = new JPanel();

		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				event.setDetectionStatus(DetectionStatus.values()[comboBoxDetectionStatus.getSelectedIndex()]);
				if (!textFieldIntensity.getText().isEmpty())
					event.setIntensity(Integer.valueOf(textFieldIntensity.getText()));
				if (manual) {
					((ManualEvent) event).setMag(textFieldMag.getText().isEmpty() ? ManualEvent.NO_MAG
							: Double.valueOf(textFieldMag.getText()));
				}
				try {
					ZejfSeis4.getEventManager().saveAll();
				} catch (FatalApplicationException e1) {
					ZejfSeis4.handleException(e1);
				}
				ZejfSeis4.getFrame().getEventsTab().updatePanel();
				EventExplorer.this.dispose();
			}
		});

		JButton delete = new JButton("Delete");
		delete.setEnabled(manual);

		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(EventExplorer.this, "Delete?", "Confirm",
						JOptionPane.YES_NO_OPTION);
				if (result == 0) {
					try {
						ZejfSeis4.getEventManager().removeEvent(event);
						ZejfSeis4.getEventManager().saveAll();
					} catch (FatalIOException e2) {
						ZejfSeis4.handleException(e2);
					}
					ZejfSeis4.getFrame().getEventsTab().updatePanel();
					EventExplorer.this.dispose();
				}
			}
		});

		buttons.add(save);
		buttons.add(delete);
		eventPanel.add(buttons, BorderLayout.SOUTH);
	}

	protected int findPeak(long start, long end) {
		double peak = 0;
		long time = start;
		while (time <= end) {
			double val = dataExplorerPanel.getDataRequest().getFilteredValue(time);
			if (Math.abs(val) > peak && val != ZejfSeis4.getDataManager().getErrVal()) {
				peak = Math.abs(val);
			}
			time += ZejfSeis4.getDataManager().getSampleTime();
		}
		return (int) peak;
	}

	public Event getEvent() {
		return event;
	}
}
