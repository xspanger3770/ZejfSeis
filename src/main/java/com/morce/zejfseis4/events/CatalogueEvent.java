package com.morce.zejfseis4.events;

import com.morce.zejfseis4.utils.GeoUtils;

public class CatalogueEvent extends Event {

	private static final long serialVersionUID = 7123289158597393382L;
	private String id;
	private long origin;
	private double lat;
	private double lon;
	private double depth;
	private double mag;
	private String magType;
	private String region;
	private long lastUpdate;

	public CatalogueEvent(String id, long origin, double lat, double lon, double depth, double mag, String magType,
			String region, long lastUpdate) {
		super();
		this.id = id;
		this.origin = origin;
		this.lat = lat;
		this.lon = lon;
		this.depth = depth;
		this.mag = mag;
		this.magType = magType;
		this.region = region;
		this.lastUpdate = lastUpdate;
	}

	@Override
	public double getDistance() {
		return GeoUtils.greatCircleDistance(lat, lon, GeoUtils.ZEJF_LAT, GeoUtils.ZEJF_LON);
	}

	@Override
	public long getOrigin() {
		return origin;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public double getDepth() {
		return depth;
	}

	public double getMag() {
		return mag;
	}

	public String getMagType() {
		return magType;
	}

	@Override
	public String getRegion() {
		return region;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public String getID() {
		return id;
	}

	public void update(CatalogueEvent event) {
		this.origin = event.origin;
		this.lat = event.lat;
		this.lon = event.lon;
		this.depth = event.depth;
		this.mag = event.mag;
		this.magType = event.magType;
		this.region = event.region;
		this.lastUpdate = event.lastUpdate;
	}

}
