package com.morce.zejfseis4.events;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.morce.zejfseis4.data.DataManager;
import com.morce.zejfseis4.exception.EventsIOException;
import com.morce.zejfseis4.exception.FatalApplicationException;
import com.morce.zejfseis4.main.ZejfSeis4;

public class EventManager {

	public static final File eventsFolder = new File(ZejfSeis4.MAIN_FOLDER, "/events/");
	public static final int PERMANENTLY_LOADED_MONTHS = 3;

	private ArrayList<EventMonth> eventMonths;
	private Object eventhMonthsSync;
	private FDSNDownloader fdsnDownloader;

	public EventManager() {
		eventMonths = new ArrayList<EventMonth>();
		eventhMonthsSync = new Object();
		try {
			load();
		} catch (EventsIOException e) {
			ZejfSeis4.errorDialog(e);
		}
		fdsnDownloader = new FDSNDownloader(this);
	}

	private void load() throws EventsIOException {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.DATE, 1);

		if (!eventsFolder.exists()) {
			eventsFolder.mkdirs();
		}

		synchronized (eventhMonthsSync) {
			for (int i = 0; i < PERMANENTLY_LOADED_MONTHS; i++) {
				loadEventMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH), true);
				c.add(Calendar.MONTH, -1);
			}
		}
	}

	private EventMonth loadEventMonth(int year, int month, boolean createNew) throws EventsIOException {
		EventMonth result = null;

		File file = getFile(year, month);
		File tempFile = getTempFile(year, month);
		int attempt = 1;
		for (File f : new File[] { file, tempFile }) {
			if (f.exists()) {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
					result = (EventMonth) in.readObject();
					in.close();
					break;
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					attempt++;
					if(attempt == 2) {
						throw new EventsIOException(e);
					}
				}
			}
		}

		if (result == null && createNew) {
			result = new EventMonth(year, month);
		}

		if (result != null) {
			synchronized (eventhMonthsSync) {
				eventMonths.add(result);
			}
		}

		return result;

	}

	public void saveAll() throws EventsIOException {
		synchronized (eventhMonthsSync) {
			for (EventMonth eventMonth : eventMonths) {
				save(eventMonth);
			}
		}
	}

	private void save(EventMonth eventMonth) throws EventsIOException {
		try {
			File file = eventMonth.getFile();
			File temp = eventMonth.getTempFile();
			if (!temp.getParentFile().exists()) {
				temp.getParentFile().mkdirs();
			}
			for (File f : new File[] { file, temp }) {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
				out.writeObject(eventMonth);
				out.close();
			}
		} catch (IOException e) {
			throw new EventsIOException(e);
		}
	}

	public EventMonth getEventMonth(int year, int month, boolean createNew) throws EventsIOException {
		synchronized (eventhMonthsSync) {
			for (EventMonth ev : eventMonths) {
				if (ev.getMonth() == month && ev.getYear() == year) {
					return ev;
				}
			}
			System.out.println("Load month id " + month);
			return loadEventMonth(year, month, createNew);
		}
	}

	public ArrayList<Event> getEvents(Calendar start, Calendar end) throws EventsIOException {
		return getEvents(start.getTimeInMillis(), end.getTimeInMillis());
	}

	public ArrayList<Event> getEvents(long start, long end) throws EventsIOException {
		ArrayList<Event> result = new ArrayList<Event>();
		Calendar endC = Calendar.getInstance();
		endC.setTimeInMillis(end);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(start);
		c.set(Calendar.DATE, 1);

		while (c.before(endC)) {
			for (Event e : getEventMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH), false).getEvents()) {
				if (e.getOrigin() >= start && e.getOrigin() <= end) {
					result.add(e);
				}
			}
			c.add(Calendar.MONTH, 1);
		}

		return result;
	}

	public static File getFile(int year, int month) {
		return new File(EventManager.eventsFolder, String.format("/%d/%s.v3", year, DataManager.MONTHS[month]));
	}

	public static File getTempFile(int year, int month) {
		return new File(EventManager.eventsFolder, String.format("/%d/_%s.v3", year, DataManager.MONTHS[month]));
	}

	public FDSNDownloader getFdsnDownloader() {
		return fdsnDownloader;
	}

	public Event getEvent(String id, long origin) throws EventsIOException {
		ArrayList<Event> candidates = getEvents(origin - 1000 * 60 * 5, origin + 1000 * 60 * 5);
		for (Event e : candidates) {
			if (e.getID().equals(id)) {
				return e;
			}
		}
		return null;
	}

	public void newEvent(Event event) throws EventsIOException {
		getEventMonth(event.getOrigin(), true).addEvent(event);
	}

	public EventMonth getEventMonth(long origin, boolean createNew) throws EventsIOException {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(origin);
		return getEventMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH), createNew);
	}

	public void removeEvent(Event event) throws EventsIOException {
		EventMonth eventMonth = getEventMonth(event.getOrigin(), false);
		if (eventMonth != null) {
			eventMonth.removeEvent(event);
		}
	}

}
