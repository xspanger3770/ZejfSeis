package zejfseis4.events;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import zejfseis4.utils.GeoUtils;

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

	public LocalDateTime getOriginDate() {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(getOrigin()), ZoneId.systemDefault());
	}

	public EventDistance getEventDistance() {
		return EventDistance.get(getDistance());
	}

	public abstract double getDepth();

	public abstract double getMag();

	public abstract String getMagType();

	public abstract String getID();

	public abstract String getRegion();

	public int getIntensity() {
		return getDetectionStatus().equals(DetectionStatus.DETECTED) ? intensity : -getDetectionStatus().ordinal();
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

	public double calculateDetectionPct() {
		return GeoUtils.probability_v6(getMag(), getDistance(), getDepth());// TODO mag types
	}

}
