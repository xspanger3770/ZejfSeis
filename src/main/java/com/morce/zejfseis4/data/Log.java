package com.morce.zejfseis4.data;

public class Log {

	private int rawValue;
	private double filteredValue;

	public Log(DataManager dataManager) {
		clear(dataManager);
	}

	public void clear(DataManager dataManager) {
		this.rawValue = dataManager.getErrVal();
		this.filteredValue = dataManager.getErrVal();
	}

	public double getFilteredValue() {
		return filteredValue;
	}

	public int getRawValue() {
		return rawValue;
	}

	public void setValues(int rawValue, double filteredValue) {
		this.rawValue = rawValue;
		this.filteredValue = filteredValue;
	}

}
