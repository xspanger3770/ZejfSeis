package com.morce.zejfseis4.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.morce.zejfseis4.data.DataHour;
import com.morce.zejfseis4.data.SimpleLog;
import com.morce.zejfseis4.exception.IncompatibleServerException;
import com.morce.zejfseis4.main.ZejfSeis4;

public class ZejfClient {

	public static final int CONNECT_TIMEOUT = 5000;
	public static final int SO_TIMEOUT = 10000;

	private Socket socket;
	private Object outputMutex = new Object();
	private Object outputQueueMutex = new Object();
	private Object inputMutex = new Object();

	private ZejfCReader reader;
	private Thread heartbeatThread;
	private Thread outputThread;

	private Semaphore outputSemaphore = new Semaphore(0);

	private Queue<String> outputQueue = new LinkedList<String>();;

	private boolean connected;

	public void connect(String ip, int port) {
		System.out.printf("Connecting to %s:%d\n", ip, port);
		ZejfSeis4.getFrame().setStatus("Connecting...");
		connected = true;
		socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);
			socket.setSoTimeout(SO_TIMEOUT);
			receiveInitialInfo(ip);
			init();
			runReader();
			ZejfSeis4.getFrame().setStatus(String.format("Connected to %s:%d", ip, port));
		} catch (Exception e) {
			e.printStackTrace();
			connected = false;
			ZejfSeis4.errorDialog(e);
			ZejfSeis4.getFrame().setStatus("Failure");
			return;
		}
	}

	private void receiveInitialInfo(String ip) throws Exception {
		String compat_version = readString();
		System.out.println(compat_version);
		int comp = Integer.valueOf(compat_version.split(":")[1]);
		if (comp != ZejfSeis4.COMPATIBILITY_VERSION) {
			throw new IncompatibleServerException();
		} else {
			System.out.println("Compatibility numbers matched");
		}
		String sample_rate = readString();
		String err_value = readString();
		String last_log_id = readString();

		System.out.println(sample_rate);
		System.out.println(err_value);
		System.out.println(last_log_id);

		int sr = Integer.valueOf(sample_rate.split(":")[1]);
		int err = Integer.valueOf(err_value.split(":")[1]);
		long lli = Long.valueOf(last_log_id.split(":")[1]);
		
		System.out.println("Received info: ");
		System.out.println("Sample rate: " + sr + "sps");
		System.out.println("Error value: " + err);
		System.out.println("Last log id: " + lli);
		
		sendStrings("realtime", lli+"");
		
		ZejfSeis4.getDataManager().load(ip, sr, err);
	}

	private String readString() throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			char ch = (char) socket.getInputStream().read();
			if (ch == '\n') {
				break;
			} else {
				result.append(ch);
			}
		}
		return result.toString();
	}

	private void init() throws IOException {
		outputThread = new Thread("Output Thread") {
			@Override
			public void run() {
				Queue<String> temp = new LinkedList<String>();
				while (true) {
					try {
						outputSemaphore.acquire();
					} catch (InterruptedException e) {
						break;
					}
					if (!connected) {
						break;
					}
					synchronized (outputQueueMutex) {
						temp.addAll(outputQueue);
						outputQueue.clear();
					}
					synchronized (outputMutex) {
						try {
							while(!temp.isEmpty()) {
								String str = temp.remove();
								socket.getOutputStream().write(String.format("%s\n", str).getBytes());
							}
							socket.getOutputStream().flush();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			
			}
		};
		
		outputThread.start();

		heartbeatThread = new Thread("ZejfClient Heartbeat Thread") {
			public void run() {
				while (true) {
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						break;
					}
					if (!connected) {
						break;
					}
					sendStrings("heartbeat");
				}
			};
		};

		heartbeatThread.start();
	}

	private void sendStrings(String... strings) {
		synchronized (outputQueueMutex) {
			for(String str:strings) {
				outputQueue.add(str);
			}
		}
		outputSemaphore.release();
	}

	private void runReader() throws IOException {
		reader = new ZejfCReader(socket.getInputStream()) {

			@Override
			public void nextLine(String line) throws Exception {
				parseLine(line);
			}

			@Override
			public void onClose() {
				if (ZejfClient.this.isConnected()) {
					ZejfClient.this.reader = null;
					ZejfClient.this.close();
				}
			}
		};
		reader.run();
	}

	private void parseLine(String line) throws Exception {
		switch (line) {
		case "heartbeat":
			// todo nothing
			break;
		case "realtime":
			synchronized (inputMutex) {
				int count = reader.nextInt();
				for (int i = 0; i < count; i++) {
					long time = reader.nextLong() * ZejfSeis4.getDataManager().getSampleTime();
					int value = reader.nextInt();
					if(value == ZejfSeis4.getDataManager().getErrVal()) {
						continue;
					}
					ZejfSeis4.getDataManager().logRealtime(new SimpleLog(time, value));
				}
			}
			break;
		case "logs":
			Queue<SimpleLog> received = new LinkedList<SimpleLog>();
			synchronized (inputMutex) {
				int count = reader.nextInt();
				for (int i = 0; i < count; i++) {
					long time = reader.nextLong() * ZejfSeis4.getDataManager().getSampleTime();
					int value = reader.nextInt();
					if(value == ZejfSeis4.getDataManager().getErrVal()) {
						continue;
					}
					received.add(new SimpleLog(time, value));
				}
			}
			ZejfSeis4.getDataManager().logRequest(received);
			break;
		}
	}

	public void close() {
		connected = false;
		if (heartbeatThread != null) {
			heartbeatThread.interrupt();
			try {
				heartbeatThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (outputThread != null) {
			outputThread.interrupt();
			try {
				outputThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (reader != null) {
			reader.close();
		}
		ZejfSeis4.getDataManager().reset();
		ZejfSeis4.getFrame().setStatus("Disconnected");
		System.out.println("Connection closed... ?");
	}

	public ZejfCReader getReader() {
		return reader;
	}

	public boolean isConnected() {
		return connected;
	}

	public void requestCheck(DataHour result) {
		sendStrings("datahour_check", result.getHourID()+"", result.getSampleCount()+"");
	}

}
