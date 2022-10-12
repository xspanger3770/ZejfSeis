package com.morce.zejfseis4.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public abstract class ZejfCReader {

	private InputStream in;

	private Thread readerThread;
	private Thread notifierThread;

	private Queue<String> queue;
	private Semaphore semaphore;
	private Object mutex;

	private boolean connected;

	public ZejfCReader(InputStream in) {
		this.in = in;
		queue = new LinkedList<>();
	}

	public void run() {
		if(connected) {
			throw new IllegalStateException("Cannot run Reader: already running!");
		}
		semaphore = new Semaphore(0);
		mutex = new Object();
		connected = true;
		readerThread = new Thread("ZejfCReader Thread") {
			@Override
			public void run() {
				runReader();
			}
		};

		readerThread.start();

		notifierThread = new Thread("ZejfCReader Notifier Thread") {
			@Override
			public void run() {
				runNotifier();
				onClose();
			}
		};

		notifierThread.start();
	}

	public void close() {
		connected = false;
		semaphore.release();
		if (readerThread != null) {
			try {
				readerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (notifierThread != null) {
			try {
				notifierThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int nextInt() throws NoSuchElementException, NumberFormatException, InterruptedException {
		semaphore.acquire();
		String line;
		synchronized (mutex) {
			line = queue.remove();
		}
		return Integer.valueOf(line);
	}

	public long nextLong() throws NoSuchElementException, NumberFormatException, InterruptedException {
		semaphore.acquire();
		String line;
		synchronized (mutex) {
			line = queue.remove();
		}
		return Long.valueOf(line);
	}

	private void runNotifier() {
		while (connected) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				connected = false;
				break;
			}
			if (!connected) {
				break;
			}
			String line;
			try {
				synchronized (mutex) {
					line = queue.remove();
				}
			} catch (NoSuchElementException e) {
				connected = false;
				break;
			}
			try {
				nextLine(line);
			} catch (Exception e) {
				e.printStackTrace();
				connected = false;
				break;
			}
		}
	}

	private void runReader() {
		byte[] buffer = new byte[2048];
		int count;
		try {
			StringBuilder line = new StringBuilder();
			while ((count = in.read(buffer, 0, 2048)) > 0 && connected) {
				for (int i = 0; i < count; i++) {
					char ch = (char) buffer[i];
					if (ch == '\n') {
						synchronized (mutex) {
							queue.add(line.toString());
						}
						semaphore.release();
						line.setLength(0);
					} else {
						line.append(ch);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
		semaphore.release();
	}

	public abstract void nextLine(String line) throws Exception;

	public abstract void onClose();

}
