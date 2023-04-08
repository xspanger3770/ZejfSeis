package zejfseis4.data;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Hopefully the last version of DataHour class Each DataHour should be about
 * 562kb (40 sps)
 * 
 * @author jakublinux
 */
public class DataHour implements Serializable {

	private static final long serialVersionUID = 2494400085954647962L;
	private int[] data;
	private long hourID;
	private int sampleRate;
	private int errorValue;

	private int sampleCount;

	private transient boolean checked;
	private transient long lastAccess;
	private transient boolean modified;

	public DataHour(long hourID, int sampleRate, int errorValue) {
		this.hourID = hourID;
		this.lastAccess = System.currentTimeMillis();
		this.sampleRate = sampleRate;
		this.errorValue = errorValue;
		this.sampleCount = 0;
		this.checked = false;
	}

	public void access() {
		this.lastAccess = System.currentTimeMillis();
	}

	public void store() {
		this.modified = false;
	}

	public void check() {
		this.checked = true;
	}

	public void unCheck() {
		this.checked = false;
	}

	private void createData() {
		data = new int[sampleRate * 60 * 60];
		for (int i = 0; i < data.length; i++) {
			data[i] = errorValue;
		}
	}

	public int getValue(long millis) {
		if(data == null) {
			return errorValue;
		}
		long remain = millis % (1000 * 60 * 60);
		int i = (int) (remain / (1000 / sampleRate));
		return data[i];
	}

	public int getValue(Calendar c) {
		return getValue(c.getTimeInMillis());
	}

	public boolean setValue(long millis, int value) {
		if(this.data == null) {
			createData();
		}
		long remain = millis % (1000 * 60 * 60);
		int i = (int) (remain / (1000 / sampleRate));
		access();
		this.modified = true;
		if (data[i] == errorValue && value != errorValue) {
			this.sampleCount++;
		}
		if (data[i] == value) {
			return false;
		} else {
			data[i] = value;
			return true;
		}
	}

	public void setValue(Calendar c, int v) {
		this.setValue(c.getTimeInMillis(), v);
	}

	public long getHourID() {
		return hourID;
	}

	public long getFirstLogTimeInMillis() {
		return hourID * 60 * 60 * 1000l;
	}

	public Calendar getFirstLog() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getFirstLogTimeInMillis());
		return c;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public boolean isModified() {
		return modified;
	}

	public boolean isChecked() {
		return checked;
	}

	public int getSampleCount() {
		return sampleCount;
	}
	
	public boolean isEmpty() {
		return getSampleCount() == 0;
	}

}
