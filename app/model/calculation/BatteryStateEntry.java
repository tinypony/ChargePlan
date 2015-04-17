package model.calculation;

public class BatteryStateEntry {

	private double charge;
	private String timestamp;
	
	public BatteryStateEntry(double charge, String timestamp) {
		this.charge = charge;
		this.timestamp = timestamp;
	}

	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
