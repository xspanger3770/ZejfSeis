package com.morce.zejfseis4.events;

import java.io.Serializable;

import com.morce.zejfseis4.utils.GeoUtils;

/**
 * Hopefully the final version of Event
 * @author jakublinux
 *
 */
public abstract class Event implements Serializable {

	private static final long serialVersionUID = 4773809413537262602L;
	private int intensity;
	private DetectionStatus detectionStatus;

	public Event() {
		this.intensity = 0;
		this.detectionStatus = DetectionStatus.UNKNOWN;
	}

	public abstract double getDistance();

	public abstract long getOrigin();

	public long minusOrigin() {
		return -getOrigin();
	}

	public EventDistance getEventDistance() {
		return EventDistance.get(getDistance());
	}

	public double getDepth() {
		return 10.0;
	}

	public double calculateDetectionPct() {
		return GeoUtils.probability_v53(getMag(), getDistance());// TODO mag types
	}

	public double sortDetection() {
		return this instanceof ManualEvent ? -999 : calculateDetectionPct();
	}

	public abstract double getMag();

	public abstract String getMagType();

	public abstract String getID();

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}
	
	public DetectionStatus getDetectionStatus() {
		return detectionStatus;
	}
	
	public void setDetectionStatus(DetectionStatus detectionStatus) {
		this.detectionStatus = detectionStatus;
	}

	public Intensity getIntensityCategory() {
		return Intensity.get(getIntensity());
	}

}
