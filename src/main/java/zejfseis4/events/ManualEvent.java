package zejfseis4.events;

import zejfseis4.utils.TravelTimeTable;

public class ManualEvent extends Event {

	private static final long serialVersionUID = -7163285823194829523L;
	private long pWave;
	private long sWave;
	private String id;
	private double mag;
	
	public static final double NO_MAG = -999;

	public ManualEvent(long pWave, long sWave) {
		super();
		this.pWave = pWave;
		this.sWave = sWave;
		this.id = "man_" + System.currentTimeMillis();
		this.mag=NO_MAG;
	}

	@Override
	public double getDistance() {
		return TravelTimeTable.getEpicenterDistance(getDepth(), (sWave - pWave) / 1000.0);
	}

	@Override
	public long getOrigin() {
		return getpWave()
				- (long) (TravelTimeTable.getPWaveTravelTime(getDepth(), TravelTimeTable.toAngle(getDistance()))
						* 1000l);
	}

	public long getpWave() {
		return pWave;
	}

	public long getsWave() {
		return sWave;
	}

	public void setpWave(long pWave) {
		this.pWave = pWave;
	}

	public void setsWave(long sWave) {
		this.sWave = sWave;
	}

	@Override
	public double getMag() {
		return mag;
	}
	
	public void setMag(double mag) {
		this.mag = mag;
	}

	@Override
	public String getMagType() {
		return "mm";
	}

	public static double calculateManualMagnitude(double dist, double intensity) {
		intensity *= 0.002;
		return Math.log10(intensity * (0.4 * Math.pow(dist, 2.1) + 1)) - 5.40;
	}

	public static void main(String[] args) {
		System.out.println(calculateManualMagnitude(16248, 13754));
		System.out.println(calculateManualMagnitude(117, 12717));
		System.out.println(calculateManualMagnitude(10, 2800));
	}

	@Override
	public String getID() {
		return id;
	}
	
	@Override
	public double getDepth() {
		return 0;
	}

	@Override
	public String getRegion() {
		return "Local";
	}

}
