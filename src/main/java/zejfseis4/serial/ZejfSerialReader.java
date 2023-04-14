package zejfseis4.serial;

import com.fazecast.jSerialComm.SerialPort;

import zejfseis4.exception.RuntimeApplicationException;
import zejfseis4.main.ZejfSeis4;

public class ZejfSerialReader {

	public static final int BAUD_RATE = 115200;

	private boolean running = false;
	private SerialPort serialPort = null;

	public void run(SerialPort port) {
		new Thread() {
			public void run() {
				try {
					openPort(port);
				} catch (Exception e) {
					ZejfSeis4.handleException(e);
				}
			};
		}.start();
	}

	private void openPort(SerialPort port) {
		running = true;
		serialPort = port;
		ZejfSeis4.getFrame().setStatus("Opening serial port...");

		if (!port.openPort()) {
			running = false;
			throw new RuntimeApplicationException("Failed to open serial port!");
		}

		port.setBaudRate(BAUD_RATE);
		port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		ZejfSeis4.getFrame().setStatus("Connected to " + port.getDescriptivePortName());

		// InputStream in = port.getInputStream();
		// StringBuilder str = new StringBuilder();
		// OutputStream out = port.getOutputStream();
		// DataOutputStream dout = new DataOutputStream(out);

	}

	public boolean isRunning() {
		return running;
	}

	public void close() {
		if (!isRunning() || serialPort == null) {
			throw new RuntimeApplicationException("Serial port not opened!");
		}

		if (!serialPort.closePort()) {
			throw new RuntimeApplicationException("Failed to close serial port!");
		}

		running = false;
	}

}
