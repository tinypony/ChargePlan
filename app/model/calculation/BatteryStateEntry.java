package model.calculation;

public class BatteryStateEntry {

	private double charge;
	private String timestamp;
	private String location;
	
	public BatteryStateEntry(double charge, String timestamp) {
		this.charge = charge;
		this.timestamp = timestamp;
	}
	
	public BatteryStateEntry(double charge, String timestamp, String location) {
		this(charge, timestamp);
		this.location = location;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
}
