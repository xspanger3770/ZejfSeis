package zejfseis4.events;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class EventMonth implements Serializable {

	private static final long serialVersionUID = 3134014099646196069L;
	private int year;
	private int month;

	private ArrayList<Event> events;

	public EventMonth(int year, int month) {
		this.year = year;
		this.month = month;
		this.events = new ArrayList<Event>();
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public File getFile() {
		return EventManager.getFile(getYear(), getMonth());
	}

	public File getTempFile() {
		return EventManager.getTempFile(getYear(), getMonth());
	}

	public synchronized void addEvent(Event event) {
		events.add(event);
	}

	public synchronized void removeEvent(Event event) {
		if (events.contains(event)) {
			events.remove(event);
		}
	}
}
