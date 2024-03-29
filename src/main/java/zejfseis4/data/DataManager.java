package zejfseis4.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.tinylog.Logger;

import zejfseis4.exception.FatalIOException;
import zejfseis4.exception.TooBigIntervalException;
import zejfseis4.main.ZejfSeis4;

public class DataManager {

	public static final int AUTOSAVE_INTERVAL_SECONDS = 5;
	public static final int PERMANENTLY_LOADED_HOURS = 24;
	public static final int STORE_TIME_MINUTES = 5;
	public static final String[] MONTHS = new String[] { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };

	public static final File INFO_FILE = new File(ZejfSeis4.MAIN_FOLDER, "/last.properties");

	private String sourceName;
	private int sampleRate;
	private int sampleTime;
	// private long lastLogId;
	private int errVal;

	private List<DataHour> dataHours;
	private List<DataRequest> dataRequests;

	private Queue<SimpleLog> realtimeQueue;
	private Queue<Queue<SimpleLog>> requestsQueue;

	private Semaphore queueSemaphore = new Semaphore(0);
	private Object logQueueMutex = new Object();

	protected Object dataHoursMutex = new Object();
	private Object dataRequestsMutex = new Object();

	private Thread autosaveThread;
	private Thread logQueueThread;

	private DataHour lastDataHour;

	public long lastRealtimeLogID;

	public DataManager() {
		sampleRate = -1;
		this.dataRequests = new ArrayList<>();
	}

	private void interruptAndJoin(Thread thread) {
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				Logger.error(e);
			}
		}
	}

	private void runQueueThread() {
		while (true) {
			try {
				queueSemaphore.acquire();
			} catch (InterruptedException e) {
				return;
			}
			processQueues();
		}
	}

	private Queue<Queue<SimpleLog>> temp_requests = new LinkedList<>();
	private Queue<SimpleLog> temp_realtime = new LinkedList<>();

	private void processQueues() {
		synchronized (logQueueMutex) {
			temp_requests.addAll(requestsQueue);
			requestsQueue.clear();
		}

		for (Queue<SimpleLog> block : temp_requests) {
			long start = -1;
			long end = -1;
			synchronized (dataHoursMutex) {
				for (SimpleLog log : block) {
					nextLog(log);
					if (start == -1) {
						start = getLogId(log.time());
					}
					if (getLogId(log.time()) > end) {
						end = getLogId(log.time());
					}
				}

			}
			if (start != -1) {
				notifyDataRequests(start, end, false);
			}
		}
		temp_requests.clear();

		synchronized (logQueueMutex) {
			temp_realtime.addAll(realtimeQueue);
			realtimeQueue.clear();
		}

		long start = -1;
		long end = -1;
		synchronized (dataHoursMutex) {
			for (SimpleLog log : temp_realtime) {
				nextLog(log);
				if (start == -1) {
					start = getLogId(log.time());
				}
				if (getLogId(log.time()) > end) {
					end = getLogId(log.time());
				}
			}
		}
		if (start != -1) {
			notifyDataRequests(start, end, true);
		}
		temp_realtime.clear();
	}

	private void notifyDataRequests(long startLogID, long endLogID, boolean realtime) {
		if (endLogID - startLogID < 0) {
			throw new IllegalStateException();
		}

		synchronized (dataRequestsMutex) {
			for (DataRequest dr : dataRequests) {
				if (!realtime) {
					if (startLogID <= dr.getEndLogID() && (endLogID >= dr.getStartLogID())) {
						dr.refill();
					}
				} else {
					dr.receiveRealtime();
				}
			}
		}
	}

	public void loadFromInfo() throws FatalIOException {
		Properties prop = new Properties();
		if (!INFO_FILE.exists()) {
			System.out.println("Last data info file doesn't exist");
			return;
		}

		try {
			prop.load(new FileInputStream(INFO_FILE));
		} catch (IOException e) {
			throw new FatalIOException("Unable to load last data info", e);
		}

		String sourceName = prop.getProperty("source_name");
		int sampleRate = Integer.valueOf(prop.getProperty("sample_rate", "-1"));
		int errVal = Integer.valueOf(prop.getProperty("err_val", "-1"));

		if (sampleRate <= 0) {
			System.err.println("Invalid sample rate: " + sampleRate);
			return;
		}

		load(sourceName, sampleRate, errVal);

	}

	public void exit() throws FatalIOException {
		interruptAndJoin(autosaveThread);
		autosaveThread = null;
		interruptAndJoin(logQueueThread);
		logQueueThread = null;
		synchronized (dataHoursMutex) {
			saveAll();
		}
		saveInfo();
	}

	private void saveInfo() throws FatalIOException {
		if (sourceName == null) {
			return;
		}
		Properties prop = new Properties();
		prop.setProperty("source_name", sourceName);
		prop.setProperty("sample_rate", sampleRate + "");
		prop.setProperty("err_val", errVal + "");
		try {
			prop.store(new FileOutputStream(INFO_FILE), "");
		} catch (IOException e) {
			throw new FatalIOException("Failed to store last data info", e);
		}
	}

	private void stopThreads() {
		interruptAndJoin(autosaveThread);
		autosaveThread = null;
		interruptAndJoin(logQueueThread);
		logQueueThread = null;

		synchronized (dataRequestsMutex) {
			for (DataRequest dataRequest : dataRequests) {
				dataRequest.stop();
			}
		}

		lastRealtimeLogID = -1;
		lastDataHour = null;

		realtimeQueue = new LinkedList<SimpleLog>();
		requestsQueue = new LinkedList<Queue<SimpleLog>>();
		queueSemaphore.drainPermits();

		temp_requests.clear();
		temp_realtime.clear();

		autosaveThread = new Thread("Autosave Thread") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(AUTOSAVE_INTERVAL_SECONDS * 1000);
					} catch (InterruptedException e) {
						break;
					}
					synchronized (dataHoursMutex) {
						cleanup();
						try {
							saveAll();
						} catch (FatalIOException e) {
							ZejfSeis4.handleException(e);
						}
						System.out.printf("Currently loaded %d DataHours\n", dataHours.size());
						System.out.println("Queue length " + realtimeQueue.size());
					}
				}
			}
		};

		logQueueThread = new Thread("Log Queue Thread") {
			@Override
			public void run() {
				runQueueThread();
			}
		};
	}

	private void startThreads() {
		autosaveThread.start();
		logQueueThread.start();

		synchronized (dataRequestsMutex) {
			for (DataRequest dataRequest : dataRequests) {
				dataRequest.restart();
			}
		}
	}

	public void load(String sourceName, int sampleRate, int errVal) throws FatalIOException {
		if (this.sourceName != null && this.sourceName.equals(sourceName) && this.sampleRate == sampleRate
				&& this.errVal == errVal) {
			return;
		}

		stopThreads();

		if (dataHours != null) {
			synchronized (dataHoursMutex) {
				saveAll();
			}
		}

		this.sourceName = sourceName;
		this.sampleRate = sampleRate;
		this.sampleTime = 1000 / sampleRate;
		this.errVal = errVal;

		synchronized (dataHoursMutex) {
			if (dataHours == null) {
				dataHours = new LinkedList<>();
			} else {
				dataHours.clear();
			}
		}

		if (dataRequests == null) {
			synchronized (dataRequestsMutex) {
				dataRequests = new LinkedList<DataRequest>();
			}
		}

		File dataFolder = getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
			System.out.println("Created " + dataFolder.getAbsolutePath());
		}

		System.out.println("Loading from " + dataFolder.getAbsolutePath());

		if(ZejfSeis4.getFrame() != null && ZejfSeis4.getFrame().getDrumTab() != null) {
			ZejfSeis4.getFrame().getDrumTab().reset();
		}
		
		saveInfo();
		startThreads();
	}

	private void cleanup() {
		long time = System.currentTimeMillis();
		Iterator<DataHour> it = dataHours.iterator();
		int count = 0;
		while (it.hasNext()) {
			DataHour dh = it.next();
			if (time - dh.getFirstLogTimeInMillis() >= PERMANENTLY_LOADED_HOURS * 60 * 60 * 1000
					&& time - dh.getLastAccess() > STORE_TIME_MINUTES * 60 * 1000) {
				it.remove();
				count++;
			}
		}
		System.out.printf("Removed %d DataHours\n", count);
	}

	private void saveAll() throws FatalIOException {
		if (dataHours == null) {
			return;
		}
		Iterator<DataHour> it = dataHours.iterator();
		int count = 0;
		while (it.hasNext()) {
			DataHour dh = it.next();
			if (dh.isModified()) {
				try {
					save(dh);
					dh.store();
					count++;
				} catch (IOException e) {
					throw new FatalIOException("Unable to save DataHour", e);
				}
			}
		}
		System.out.printf("Saved %d DataHours\n", count);
	}

	private void save(DataHour dh) throws IOException {
		File temp = getDataHourTempFile(dh);
		File file = getDataHourFile(dh);
		if (!temp.getParentFile().exists()) {
			temp.getParentFile().mkdirs();
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(temp));
		out.writeObject(dh);
		out.close();
		file.delete();
		temp.renameTo(file);
		temp.delete();
	}

	private DataHour loadDataHour(long hourId) throws FatalIOException {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getMillisFromHourId(hourId));
		File file = getDataHourFile(c);
		System.out.println("Try to load " + file.getAbsolutePath());
		if (file.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				DataHour dh = (DataHour) in.readObject();
				in.close();
				if (dh.getHourID() != hourId) {
					System.err.println("DH id doesnt match!");
					return null;
				}
				return dh;
			} catch (ClassNotFoundException e) {
				throw new FatalIOException(String.format("DataHour file probably corrupt: %s", file.getAbsolutePath()),
						e);
			} catch (IOException e) {
				throw new FatalIOException(String.format("Unable to load DataHour: %s", file.getAbsolutePath()), e);
			}
		}
		return null;
	}

	public Queue<DataHour> getDataHours(long start, long end) throws TooBigIntervalException, FatalIOException {
		Queue<DataHour> result = new LinkedList<DataHour>();
		long startHourID = getHourId(getMillis(start));
		long endHourID = getHourId(getMillis(end));

		if (endHourID - startHourID > 5 * 24) {
			throw new TooBigIntervalException(
					String.format("Too big interval in function getDataHours (%s)", endHourID - startHourID));
		}
		for (long hourID = startHourID; hourID <= endHourID; hourID++) {
			DataHour dh;
			dh = getDataHour(hourID, true, true);
			if (dh != null) {
				result.add(dh);
			}
		}
		return result;
	}

	public int getLog(long logId) {
		long millis = getMillis(logId);
		DataHour dh;
		try {
			dh = getDataHour(getHourId(millis), true, true);
		} catch (FatalIOException e) {
			Logger.error(e);
			return errVal;
		}
		if (dh == null) {
			return errVal;
		}
		dh.access();
		return dh.getValue(millis);
	}

	private boolean nextLog(SimpleLog log) {
		DataHour dh;
		try {
			dh = getDataHour(getHourId(log.time()), true, true);
		} catch (FatalIOException e) {
			Logger.error(e);
			return false;
		}
		if (dh == null) {
			return false;
		}
		dh.access();
		dh.setValue(log.time(), log.value());

		if (getLogId(log.time()) > lastRealtimeLogID) {
			lastRealtimeLogID = getLogId(log.time());
		}

		return true;
	}

	private DataHour getDataHour(long hourId, boolean loadFromFile, boolean createNew) throws FatalIOException {
		if (dataHours == null) {
			return null;
		}
		DataHour result = null;
		boolean add = false;

		if (lastDataHour != null && lastDataHour.getHourID() == hourId) {
			result = lastDataHour;
		}

		if (result == null) {
			for (DataHour dh : dataHours) {
				if (dh.getHourID() == hourId) {
					result = dh;
				}
			}
		}

		if (result == null && loadFromFile) {
			DataHour dh = loadDataHour(hourId);
			if (dh != null) {
				result = dh;
				add = true;
			}
		}

		if (result == null && createNew) {
			DataHour dh = new DataHour(hourId, sampleRate, errVal);
			result = dh;
			add = true;
		}

		if (add) {
			dataHours.add(result);
			notifyDataRequests(getLogId(getMillisFromHourId(hourId)), getLogId(getMillisFromHourId(hourId + 1)) - 1,
					false);
		}

		if (result != null && !result.isChecked()) {
			result.check();
			ZejfSeis4.getClient().requestCheck(result);
		}

		lastDataHour = result;

		return result;
	}

	private File getDataHourFile(DataHour dataHour) {
		return getDataHourFile(dataHour.getFirstLog());
	}

	private File getDataHourFile(Calendar c) {
		String str = String.format("/%d/%s/%02d/%02dH_%d.z4", c.get(Calendar.YEAR), MONTHS[c.get(Calendar.MONTH)],
				c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), getHourId(c.getTimeInMillis()));
		return new File(getDataFolder(), str);
	}

	private File getDataHourTempFile(DataHour dataHour) {
		return getDataHourTempFile(dataHour.getFirstLog());
	}

	private File getDataHourTempFile(Calendar c) {
		String str = String.format("/%d/%s/%02d/temp_%02dH_%d.z4", c.get(Calendar.YEAR), MONTHS[c.get(Calendar.MONTH)],
				c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), getHourId(c.getTimeInMillis()));
		return new File(getDataFolder(), str);
	}

	public void logRealtime(SimpleLog log) {
		synchronized (logQueueMutex) {
			realtimeQueue.add(log);
		}
		queueSemaphore.release();
	}

	public void logRequest(Queue<SimpleLog> logs) {
		synchronized (logQueueMutex) {
			requestsQueue.add(logs);
		}
		queueSemaphore.release();
	}

	public void registerDataRequest(DataRequest dataRequest) {
		synchronized (dataRequestsMutex) {
			dataRequests.add(dataRequest);
		}
		dataRequest.runWorkerThread();
	}

	public void unregisterDataRequest(DataRequest dataRequest) {
		synchronized (dataRequestsMutex) {
			dataRequest.stop();
			dataRequests.remove(dataRequest);
		}
	}

	public File getDataFolder() {
		return new File(ZejfSeis4.MAIN_FOLDER, String.format("/%s_%dsps", sourceName, sampleRate));
	}

	public long getLogId(long millis) {
		return millis / sampleTime;
	}

	public long getMillis(long logId) {
		return logId * sampleTime;
	}

	public long getMillisFromHourId(long hourId) {
		return hourId * 1000 * 60 * 60l;
	}

	public long getHourId(long millis) {
		return millis / (1000 * 60 * 60l);
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getSampleTime() {
		return sampleTime;
	}

	public int getErrVal() {
		return errVal;
	}

	public void reset() {
		synchronized (dataHoursMutex) {
			for (DataHour dh : dataHours) {
				dh.unCheck();
			}
		}
	}

	public boolean isLoaded() {
		return sampleRate != -1;
	}

}
