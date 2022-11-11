package com.morce.zejfseis4.events;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.morce.zejfseis4.data.DataManager;
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
		load();
		fdsnDownloader = new FDSNDownloader(this);
	}

	private void load() {
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

	private EventMonth loadEventMonth(int year, int month, boolean createNew) {
		File f = getFile(year, month);
		if (f.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));

				EventMonth eventMonth = (EventMonth) in.readObject();
				synchronized (eventhMonthsSync) {
					eventMonths.add(eventMonth);
				}
				in.close();
				return eventMonth;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (createNew) {
				EventMonth eventMonth = new EventMonth(year, month);
				synchronized (eventhMonthsSync) {
					eventMonths.add(eventMonth);
				}
				return eventMonth;
			} else {
				return null;
			}
		}
		return null;
	}

	public void saveAll() {
		synchronized (eventhMonthsSync) {
			for (EventMonth eventMonth : eventMonths) {
				save(eventMonth);
			}
		}
	}

	private void save(EventMonth eventMonth) {
		try {
			File temp = eventMonth.getTempFile();
			File file = eventMonth.getFile();
			if (!temp.getParentFile().exists()) {
				temp.getParentFile().mkdirs();
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(temp));
			out.writeObject(eventMonth);
			out.close();
			file.delete();
			temp.renameTo(file);
			temp.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EventMonth getEventMonth(int year, int month, boolean createNew) {
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

	public ArrayList<Event> getEvents(Calendar start, Calendar end) {
		return getEvents(start.getTimeInMillis(), end.getTimeInMillis());
	}

	public ArrayList<Event> getEvents(long start, long end) {
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

	public Event getEvent(String id, long origin) {
		ArrayList<Event> candidates = getEvents(origin - 1000 * 60 * 5, origin + 1000 * 60 * 5);
		for (Event e : candidates) {
			if (e.getID().equals(id)) {
				return e;
			}
		}
		return null;
	}

	public void newEvent(Event event) {
		getEventMonth(event.getOrigin(), true).addEvent(event);
	}

	public EventMonth getEventMonth(long origin, boolean createNew) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(origin);
		return getEventMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH), createNew);
	}

	public void removeEvent(Event event) {
		EventMonth eventMonth = getEventMonth(event.getOrigin(), false);
		if (eventMonth != null) {
			eventMonth.removeEvent(event);
		}
	}

}
