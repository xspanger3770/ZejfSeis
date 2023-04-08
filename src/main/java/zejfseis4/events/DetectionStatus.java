package zejfseis4.events;

public enum DetectionStatus {

	UNKNOWN, NOT_DETECTED, BROKEN, NOISE, DETECTED;
	
	@Override
	public String toString() {
		String str = super.toString().toLowerCase().replace('_', ' ');
		String cap = str.substring(0, 1).toUpperCase() + str.substring(1);
		return cap;
	}
	
}
