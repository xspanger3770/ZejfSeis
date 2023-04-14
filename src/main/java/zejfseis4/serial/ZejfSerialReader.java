package zejfseis4.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;

import zejfseis4.data.SimpleLog;
import zejfseis4.exception.FatalIOException;
import zejfseis4.exception.RuntimeApplicationException;
import zejfseis4.main.Settings;
import zejfseis4.main.ZejfSeis4;

public class ZejfSerialReader {

	public static final int BAUD_RATE = 115200;
	public static final int ERR_VALUE = Integer.MIN_VALUE;
	

	private static final byte plus = '+';
	private static final byte minus = '-';
	private static final int SHIFT_CHECK = 80;
	private static final int CALIBRATION_TRESHOLD = 1500 * 10;
	
	private long first_log_id;
	private int first_log_num;
	private int last_log_num;
	private boolean calibrating;
	private int sample_time_ms;
	private long count_diffs;
	private long sum_diffs;
	private boolean last_set;
	private double last_avg_diff;

	private boolean running = false;
	private SerialPort serialPort = null;

	boolean initialized = false;
	private OutputStream outputStream;

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

	private void openPort(SerialPort port) throws FatalIOException {
		running = true;
		initialized = false;
		serialPort = port;
		ZejfSeis4.getFrame().setStatus("Opening serial port...");

		if (!port.openPort()) {
			running = false;
			throw new RuntimeApplicationException("Failed to open serial port!");
		}

		port.setBaudRate(BAUD_RATE);
		port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		ZejfSeis4.getFrame().setStatus("Connected to " + port.getDescriptivePortName() + ", waiting for data...");

		ZejfSeis4.getDataManager().load(port.getDescriptivePortName(), Settings.SERIAL_PORT_SAMPLE_RATE, ERR_VALUE);

		outputStream = port.getOutputStream();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		runReader(port);

	}

	private void diff_control(long diff, int shift) throws IOException {
		count_diffs++;
		sum_diffs += diff;
		if (count_diffs == SHIFT_CHECK) {
			double avg_diff = sum_diffs / (double) count_diffs;
			count_diffs = 0;
			sum_diffs = 0;

			if (last_set) {
				if (calibrating && Math.abs(avg_diff) < CALIBRATION_TRESHOLD) {
					calibrating = false;
					System.out.println("Calibration done, you can now see the data.");
					ZejfSeis4.getFrame().setStatus("Connected to " + serialPort.getDescriptivePortName());
				}

				double change = avg_diff - last_avg_diff;
				double goal = calibrating ? -avg_diff / 4.0 : -avg_diff / 5.0;
				int shift_goal = (shift + (int) Math.round((goal - change) / SHIFT_CHECK));
				int conf = (shift_goal - shift);

				if (calibrating) {
					conf *= 1.50;
				}

				if (conf > 0) {
					for (int i = 0; i < conf / 3 + 1; i++) {
						outputStream.write(plus);
					}
				}
				if (conf < 0) {
					for (int i = 0; i < -conf / 3 + 1; i++) {
						outputStream.write(minus);
					}
				}

				if (calibrating) {
					ZejfSeis4.getFrame().setStatus("Connected to " + serialPort.getDescriptivePortName()
							+ ", Calibrating " + (int) (avg_diff / 1000.0) + " ms");
				}

				System.out.printf("avg diff: %.5fms, changed by %.2fms, goal: %.2fms, shift: %d, target shift: %d\n",
						avg_diff / 1000.0, change / 1000.0, goal / 1000.0, shift, shift_goal);
			}

			last_avg_diff = avg_diff;
			last_set = true;
		}
	}

	void next_sample(int shift, int log_num, int value) {
		long time = System.currentTimeMillis() * 1000l;
		if (first_log_id == -1) {
			first_log_id = time / (1000 * sample_time_ms) + 1;
			ZejfSeis4.getFrame().setStatus("Connected to " + serialPort.getDescriptivePortName() + ", Calibrating...");
			System.out.println("calibrating " + (first_log_id * 1000 * sample_time_ms - time) + " us\n");
			first_log_num = log_num;
		} else {
			if (log_num == last_log_num) {
				return;
			} else if (log_num < last_log_num) { // log num overflow
				first_log_id = first_log_id + (last_log_num - first_log_num) + 1;
				first_log_num = log_num;
			} else if (log_num - last_log_num > 1) {
				System.out.println("ERR COMM GAP!\n");
			}

			long expected_time = (first_log_id + (log_num - first_log_num)) * sample_time_ms * 1000;
			long diff = time - expected_time;
			try {
				diff_control(diff, shift);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!calibrating) {
			ZejfSeis4.getDataManager()
					.logRealtime(new SimpleLog((first_log_id + (log_num - first_log_num)) * sample_time_ms, value));
		}
		last_log_num = log_num;
	}

	boolean decode(String str) {
		try {
			Scanner scanner = new Scanner(str);
			scanner.useDelimiter("[^-\\d]+"); // Use non-digit characters as delimiters
			int shift = scanner.nextInt();
			int log_num = scanner.nextInt();
			int value = scanner.nextInt();
			scanner.close();
			next_sample(shift, log_num, value);
		} catch (NoSuchElementException e) {
			return false;
		}

		return true;
	}

	private void nextLine(String str) {
		if (!initialized) {
			try {
				outputStream.write((Arrays.binarySearch(Settings.SAMPLE_RATES, Settings.SERIAL_PORT_SAMPLE_RATE) + "\n")
						.getBytes());
			} catch (IOException e) {
				throw new RuntimeApplicationException("Serial port failure", e);
			}
			initialized = true;
		}

		decode(str);
	}

	private void runReader(SerialPort port) {
		count_diffs = 0;
		sum_diffs = 0;
		first_log_id = -1;
		first_log_num = 0;
		last_log_num = 0;
		calibrating = true;
		last_set = false;
		last_avg_diff = 0;
		sample_time_ms = 1000 / Settings.SERIAL_PORT_SAMPLE_RATE;

		InputStream in = port.getInputStream();
		StringBuilder str = new StringBuilder();
		char ch;
		while (true) {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			String s = String.valueOf(ch);
			if (!s.equals("\r")) {
				if (s.equals("\n")) {
					nextLine(str.toString());
					str.setLength(0);
				} else {
					str.append(s);
				}
			}
		}

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

		ZejfSeis4.getFrame().setStatus("Idle");

		running = false;
	}

}
