package com.morce.zejfseis4.events;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.utils.NamedThreadFactory;

public class FDSNDownloader {

	private static final double TRESHOLD = 4.0 / 100.0;

	protected static final int HOURS_BACKWARD = 24 * 1;

	private EventManager eventManager;

	private static File lastDownloadFile = new File(EventManager.eventsFolder, "lastDownload.dat");
	private long lastDownload;

	public FDSNDownloader(EventManager eventManager) {
		this.eventManager = eventManager;
		load();
		run();
	}

	private void load() {
		try {
			if (lastDownloadFile.exists()) {
				DataInputStream in = new DataInputStream(new FileInputStream(lastDownloadFile));
				lastDownload = in.readLong();
				in.close();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		lastDownload = System.currentTimeMillis();
	}

	private void run() {
		ScheduledExecutorService execEvents = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("Events Downloader"));
		execEvents.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					FDSNDownloader.this.update();
				} catch (Exception e) {
					System.err.println("Exception occured in events downloader");
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.MINUTES);
	}

	private void update() {
		try {
			Calendar start = Calendar.getInstance();
			start.setTimeInMillis(lastDownload - (1000 * 60 * 60 * HOURS_BACKWARD));
			ArrayList<CatalogueEvent> events = downloadEvents(start, null, 1000, -999);
			lastDownload = System.currentTimeMillis();

			try {
				DataOutputStream out = new DataOutputStream(new FileOutputStream(lastDownloadFile));
				out.writeLong(lastDownload);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (CatalogueEvent event : events) {
				processEvent(event);
			}

			ZejfSeis4.getEventManager().saveAll();
			ZejfSeis4.getFrame().getEventsTab().updatePanel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void downloadWholeMonth(Calendar c) throws Exception{
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(c.getTimeInMillis());
		
		start.set(Calendar.DATE, 1);
		start.set(Calendar.HOUR, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		
		Calendar end = Calendar.getInstance();
		end.setTime(start.getTime());
		end.add(Calendar.MONTH, 1);
		
		while(start.before(end)) {
			System.out.println("From "+start.getTimeInMillis()+" to "+end.getTimeInMillis());
			ArrayList<CatalogueEvent> events = downloadEvents(start, null, 1000, -999);
			if(events.isEmpty()) {
				break;
			}
			for (CatalogueEvent event : events) {
				if(event.getOrigin() > end.getTimeInMillis() || event.getOrigin() > System.currentTimeMillis()) {
					break;
				}
				processEvent(event);
			}
			
			start.setTimeInMillis(events.get(events.size() -1).getOrigin());
			ZejfSeis4.getFrame().getEventsTab().updatePanel();
			ZejfSeis4.getEventManager().saveAll();
		}
	}

	private void processEvent(CatalogueEvent event) {
		CatalogueEvent original = (CatalogueEvent) getEventManager().getEvent(event.getID(), event.getOrigin());
		boolean exists = original != null;
		boolean detectable = event.calculateDetectionPct() >= TRESHOLD;
		if (exists) {
			boolean changed = original.getLastUpdate() < event.getLastUpdate();
			if (changed) {
				original.update(event);
			}
		} else if (detectable) {
			getEventManager().newEvent(event);
		}
	}

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

	// From ZejfSeis 1
	public static ArrayList<CatalogueEvent> downloadEvents(Calendar start, Calendar end, int limit, double minMag)
			throws Exception {
		ArrayList<CatalogueEvent> list = new ArrayList<CatalogueEvent>();

		if (start != null && end == null) {
			start.add(Calendar.SECOND, 1);// ?
		}
		String start_str = start == null ? "" : "&start=" + format.format(start.getTime());
		String end_str = end == null ? "" : "&end=" + format.format(end.getTime());
		URL url = new URL("https://www.seismicportal.eu/fdsnws/event/1/query?"
				+ ((start != null && end == null) ? "orderby=time-asc" : "") + "&limit=" + limit + start_str + end_str
				+ "&format=json" + (minMag == -999 ? "" : "&minmag=" + minMag));
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		System.out.println("URL: " + url.toString());
		StringBuilder result = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			result.append(inputLine);
		}
		in.close();

		JSONObject obj = null;
		try {
			obj = new JSONObject(result.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSONArray array = obj.getJSONArray("features");
		for (int i = 0; i < array.length(); i++) {
			JSONObject properties = array.getJSONObject(i).getJSONObject("properties");
			list.add(_decode(properties));
		}
		if (start != null && end == null) {
			// Collections.sort(list); //TODO
		}
		return list;
	}

	public static CatalogueEvent _decode(JSONObject properties) throws Exception {
		double lat = properties.getDouble("lat");
		double lon = properties.getDouble("lon");
		double depth = properties.getDouble("depth");
		double mag = properties.getDouble("mag");
		String magType = properties.getString("magtype");
		String region = properties.getString("flynn_region");
		String emsc_id = properties.getString("source_id");

		String time = properties.getString("time");
		String lastUpdateStr = properties.getString("lastupdate");

		long origin = Instant.parse(time).toEpochMilli();
		long lastUpdate = Instant.parse(lastUpdateStr).toEpochMilli();

		return new CatalogueEvent(emsc_id, origin, lat, lon, depth, mag, magType, region, lastUpdate);
	}

	public static void main(String[] args) {
		Calendar a = Calendar.getInstance();
		a.setTimeInMillis(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
		try {
			System.out.println(downloadEvents(a, null, 1000, -999));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EventManager getEventManager() {
		return eventManager;
	}

}
