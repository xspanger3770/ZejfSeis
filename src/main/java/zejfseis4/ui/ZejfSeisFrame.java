package zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import com.fazecast.jSerialComm.SerialPort;

import zejfseis4.exception.FatalIOException;
import zejfseis4.main.Settings;
import zejfseis4.main.ZejfSeis4;
import zejfseis4.utils.SpringUtilities;

public class ZejfSeisFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JLabel lblStatus;

	private String status;
	private Semaphore semaphore;
	private RealtimeTab realtimeTab;
	private DrumTab drumTab;
	private EventsTab eventsTab;

	public ZejfSeisFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(ZejfSeisFrame.class.getResource("icon.png")).getImage());
	}

	public void createFrame() {
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setPreferredSize(new Dimension(900, 600));
		contentPane.setSize(contentPane.getPreferredSize());
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		realtimeTab = new RealtimeTab();
		tabbedPane.addTab("Realtime", realtimeTab);

		drumTab = new DrumTab();
		tabbedPane.addTab("Drum", drumTab);

		eventsTab = new EventsTab();
		tabbedPane.addTab("Events", eventsTab);

		status = "Loading...";
		semaphore = new Semaphore(0);

		lblStatus = new JLabel(status);
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblStatus.setBorder(new EmptyBorder(2, 4, 2, 2));
		contentPane.add(lblStatus, BorderLayout.SOUTH);

		JMenuBar bar = new JMenuBar();

		JMenu connectionMenu = new JMenu("Connection");

		JMenuItem menuSerial = new JMenuItem("Serial Port", KeyEvent.VK_P);
		menuSerial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runSerial();
			}
		});

		connectionMenu.add(menuSerial);

		JMenuItem menuServer = new JMenuItem("Server", KeyEvent.VK_S);
		menuServer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runSocket();
			}
		});

		connectionMenu.add(menuServer);

		JMenu settingsMenu = new JMenu("Settings");
		
		JMenuItem seismometer = new JMenuItem("Seismometer", KeyEvent.VK_S);
		seismometer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				seismometerSettings();
			}
		});

		JMenuItem realtime = new JMenuItem("Realtime", KeyEvent.VK_R);
		realtime.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				realtimeSettings();
			}
		});

		JMenuItem drumMenuItem = new JMenuItem("Drum", KeyEvent.VK_D);
		drumMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				drumSettings();
			}
		});

		JMenuItem serialPortItem = new JMenuItem("Serial Port", KeyEvent.VK_P);
		serialPortItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				serialPortSettings();
			}
		});
		
		settingsMenu.add(seismometer);
		settingsMenu.add(realtime);
		settingsMenu.add(drumMenuItem);
		settingsMenu.add(serialPortItem);

		bar.add(connectionMenu);
		bar.add(settingsMenu);

		JMenu filters = new JMenu("Filters");

		List<DefaultFilter> defaultFilters = new ArrayList<>();

		defaultFilters.add(new DefaultFilter("Default", 20, 0.5));
		defaultFilters.add(new DefaultFilter("Full", 20, 0.0));
		defaultFilters.add(new DefaultFilter("Regional", 6, 1));
		defaultFilters.add(new DefaultFilter("Distant", 2, 0.5));
		defaultFilters.add(new DefaultFilter("S Waves", 0.08, 0.01));
		defaultFilters.add(new DefaultFilter("Calibration", 0.01, 0.0));

		for (DefaultFilter defaultFilter : defaultFilters) {
			JMenuItem item = new JMenuItem(String.format("%s (%.2fHz - %.2fHz)", defaultFilter.name(),
					defaultFilter.min(), defaultFilter.max()));
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setFilters(defaultFilter.max(), defaultFilter.min());
				}
			});
			filters.add(item);
		}
		
		JMenuItem customFilter = new JMenuItem("Custom", KeyEvent.VK_C);
		customFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				filterSettings();
			}
		});
		
		filters.add(customFilter);

		bar.add(filters);

		setJMenuBar(bar);
		setMinimumSize(new Dimension(400, 300));
		setTitle("ZejfSeis " + ZejfSeis4.VERSION);
		pack();
		setLocationRelativeTo(null);

		new Thread("Status update thread") {
			public void run() {
				while (true) {
					try {
						semaphore.acquire();
					} catch (InterruptedException e) {
						break;
					}
					lblStatus.setText(status);
				}
			};
		}.start();
	}
	

	protected void serialPortSettings() {
		JLabel l = new JLabel("Sample Rate:", JLabel.TRAILING);
		JPanel p = new JPanel(new SpringLayout());
		
		p.add(l);
		JComboBox<String> line = new JComboBox<String>();
		for (int sr : Settings.SAMPLE_RATES) {
			line.addItem(sr+" Hz");
		}
		line.setSelectedIndex(Arrays.binarySearch(Settings.SAMPLE_RATES, Settings.SERIAL_PORT_SAMPLE_RATE));
		p.add(line);

		// JLabel l2 = new JLabel("Antialiasing:", JLabel.TRAILING);
		// p.add(l2);
		// JCheckBox checkBox = new JCheckBox();
		// checkBox.setSelected(Settings.ANTIALIAS);
		// p.add(checkBox);
		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Serial Port Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				Settings.SERIAL_PORT_SAMPLE_RATE = Settings.SAMPLE_RATES[line.getSelectedIndex()];
				Settings.saveProperties();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (FatalIOException e) {
				ZejfSeis4.handleException(e);
			}
		}
	}


	protected void seismometerSettings() {
		String[] labels = { "Latitude (˚N):", "Longitude(˚E):", "Background noise:" };
		int numPairs = labels.length;
		JTextField[] fields = new JTextField[numPairs];
		String[] values = new String[numPairs];
		values[0] = Settings.LOCATION_LATITUDE + "";
		values[1] = Settings.LOCATION_LONGITUDE + "";
		values[2] = Settings.NOISE_LEVEL + "";
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(3);
			textField.setText(values[i]);
			l.setLabelFor(textField);
			p.add(textField);
			fields[i] = textField;
		}
		SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Seismometer Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				double lat = Double.valueOf(fields[0].getText());
				double lon = Double.valueOf(fields[1].getText());
				int noise = Integer.valueOf(fields[2].getText());
				if (noise < 0) {
					throw new IllegalArgumentException("noise must be > 0!");
				}
				Settings.LOCATION_LATITUDE = lat;
				Settings.LOCATION_LONGITUDE = lon;
				Settings.NOISE_LEVEL = noise;
				Settings.saveProperties();
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (FatalIOException e) {
				ZejfSeis4.handleException(e);
			}
		}

	}

	protected void realtimeSettings() {
		String[] labels = { "Spectrogram gain: ", "Spectrogram window: ", "Spectrogram maximum frequency: " };
		int numPairs = labels.length;
		JTextField[] fields = new JTextField[numPairs];
		String[] values = new String[numPairs];
		values[0] = Settings.SPECTRO_GAIN % 1.0 == 0.0 ? (int) Settings.SPECTRO_GAIN + "" : Settings.SPECTRO_GAIN + "";
		values[1] = Settings.WINDOW + "";
		values[2] = Settings.SPECTRO_MAX_FREQUENCY + "";
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(3);
			textField.setText(values[i]);
			l.setLabelFor(textField);
			p.add(textField);
			fields[i] = textField;
		}

		JLabel l = new JLabel("Realtime Duration:", JLabel.TRAILING);
		p.add(l);
		JComboBox<String> line = new JComboBox<String>();
		for (String str : Settings.DURATIONS_NAMES) {
			line.addItem(str);
		}
		line.setSelectedIndex(Arrays.binarySearch(Settings.DURATIONS, Settings.REALTIME_DURATION_SECONDS));
		p.add(line);

		// JLabel l2 = new JLabel("Antialiasing:", JLabel.TRAILING);
		// p.add(l2);
		// JCheckBox checkBox = new JCheckBox();
		// checkBox.setSelected(Settings.ANTIALIAS);
		// p.add(checkBox);
		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, numPairs + 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Realtime Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				Double spectro_gain = Double.valueOf(fields[0].getText());
				Integer window = Integer.valueOf(fields[1].getText());
				Double spectro_max_freq = Double.valueOf(fields[2].getText());
				Settings.SPECTRO_GAIN = spectro_gain;
				Settings.WINDOW = window;
				Settings.REALTIME_DURATION_SECONDS = Settings.DURATIONS[line.getSelectedIndex()];
				// Settings.ANTIALIAS = checkBox.isSelected();
				Settings.SPECTRO_MAX_FREQUENCY = spectro_max_freq;
				Settings.saveProperties();

				realtimeTab.getRealtimeGraphPanel().updateDuration();
				realtimeTab.getSpectrogramPanel().updateDuration();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (FatalIOException e) {
				ZejfSeis4.handleException(e);
			}
		}
	}

	protected void filterSettings() {
		String[] labels = { "Minimum Frequency (Hz):", "Maximum Frequency (Hz):" };
		int numPairs = labels.length;
		JTextField[] fields = new JTextField[numPairs];
		String[] values = new String[numPairs];
		values[0] = Settings.MIN_FREQUENCY + "";
		values[1] = Settings.MAX_FREQUENCY + "";
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(3);
			textField.setText(values[i]);
			l.setLabelFor(textField);
			p.add(textField);
			fields[i] = textField;
		}
		SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Filter Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				double min = Double.valueOf(fields[0].getText());
				double max = Double.valueOf(fields[1].getText());
				if (min > max) {
					throw new IllegalArgumentException("min > max !");
				}
				if (min < 0 || max < 0) {
					throw new IllegalArgumentException("Invalid values!");
				}
				if (max == 0) {
					throw new IllegalArgumentException("Max cannot be 0!");
				}
				setFilters(max, min);
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private void setFilters(double max, double min) {
		Settings.MAX_FREQUENCY = max;
		Settings.MIN_FREQUENCY = min;
		try {
			Settings.saveProperties();
		} catch (FatalIOException e) {
			ZejfSeis4.handleException(e);
		}

		realtimeTab.getRealtimeGraphPanel().updateFilter();
		realtimeTab.getSpectrogramPanel().updateFilter();
		drumTab.updateFilter();
	}

	protected void drumSettings() {
		String[] labels = { "Gain: ", "Decimate: " };
		int numPairs = labels.length;
		JTextField[] fields = new JTextField[numPairs];
		String[] values = new String[numPairs];
		values[0] = Settings.DRUM_GAIN % 1.0 == 0.0 ? (int) Settings.DRUM_GAIN + "" : Settings.DRUM_GAIN + "";
		values[1] = Settings.DECIMATE + "";
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(3);
			textField.setText(values[i]);
			l.setLabelFor(textField);
			p.add(textField);
			fields[i] = textField;
		}
		JLabel l = new JLabel("Line Duration:", JLabel.TRAILING);
		p.add(l);
		JComboBox<String> line = new JComboBox<String>();
		for (int i : Settings.DRUM_SPACES) {
			line.addItem(i + " Minutes");
		}
		line.setSelectedIndex(Settings.DRUM_SPACE_INDEX);
		p.add(line);
		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, numPairs + 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Drum Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				Double gain = Double.valueOf(fields[0].getText());
				Settings.DRUM_GAIN = gain;
				Settings.DRUM_SPACE_INDEX = line.getSelectedIndex();
				Settings.DECIMATE = Integer.valueOf(fields[1].getText());
				Settings.saveProperties();
			} catch (FatalIOException e) {
				ZejfSeis4.handleException(e);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private boolean checkCurrentConnection() {
		if (ZejfSeis4.getClient() != null && ZejfSeis4.getClient().isConnected()) {
			int result = JOptionPane.showConfirmDialog(this,
					"Disconnect from " + Settings.ADDRESS + ":" + Settings.PORT + "?", "Server",
					JOptionPane.YES_NO_OPTION);
			if (result != 0) {
				return false;
			}

			ZejfSeis4.getClient().close();
		}
		
		if(ZejfSeis4.getSerialReader() != null && ZejfSeis4.getSerialReader().isRunning()) {
			int result = JOptionPane.showConfirmDialog(this,
					"Close serial port?", "Serial Port",
					JOptionPane.YES_NO_OPTION);
			if (result != 0) {
				return false;
			}

			ZejfSeis4.getSerialReader().close();

		}
		return true;
	}

	private SerialPort selectPort() {
		if (SerialPort.getCommPorts().length == 0) {
			JOptionPane.showMessageDialog(this, "No ports found.", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		JPanel p = new JPanel(new SpringLayout());
		JLabel l = new JLabel("Select Port:");
		p.add(l);
		JComboBox<String> line = new JComboBox<String>();
		for (SerialPort ser : SerialPort.getCommPorts()) {
			line.addItem(ser.getDescriptivePortName());
		}
		p.add(line);
		SpringUtilities.makeCompactGrid(p, 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Port Selection", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				return SerialPort.getCommPorts()[line.getSelectedIndex()];
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	public void runSerial() {
		if (!checkCurrentConnection()) {
			return;
		}

		SerialPort port = selectPort();
		if(port != null) {
			ZejfSeis4.getSerialReader().run(port);
		}
	}

	public void runSocket() {
		if (!checkCurrentConnection()) {
			return;
		}

		if (getAddress()) {
			ZejfSeis4.getClient().connect(Settings.ADDRESS, Settings.PORT);
		}
	}

	private boolean getAddress() {
		String[] labels = { "Address:", "Port:" };
		int numPairs = labels.length;
		JTextField[] fields = new JTextField[numPairs];
		String[] values = new String[numPairs];
		values[0] = Settings.ADDRESS + "";
		values[1] = Settings.PORT + "";
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			p.add(l);
			JTextField textField = new JTextField(3);
			textField.setText(values[i]);
			l.setLabelFor(textField);
			p.add(textField);
			fields[i] = textField;
		}
		SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		if (JOptionPane.showConfirmDialog(this, p, "Select Server", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == 0) {
			try {
				String address = fields[0].getText();
				int port = Integer.valueOf(fields[1].getText());
				Settings.ADDRESS = address;
				Settings.PORT = port;
				Settings.saveProperties();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				getAddress();
			} catch (FatalIOException e) {
				ZejfSeis4.handleException(e);
			}
			return true;
		}
		return false;
	}

	public void setStatus(String status) {
		this.status = status;
		semaphore.release();
	}

	public EventsTab getEventsTab() {
		return eventsTab;
	}

}
