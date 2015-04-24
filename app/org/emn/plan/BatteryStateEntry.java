package org.emn.plan;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BatteryStateEntry {

	private double charge;
	private Date timestamp;
	private String location;
	
	public BatteryStateEntry(double charge, Date timestamp) {
		this.charge = charge;
		this.timestamp = timestamp;
	}
	
	public BatteryStateEntry(double charge, Date timestamp, String location) {
		this(charge, timestamp);
		this.location = location;
	}
	
	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	
	public String getSimpleTimestamp() {
		return (new SimpleDateFormat("HHmm")).format(getTimestamp());
	}
	
	public String getStringTimestamp() {
		return (new SimpleDateFormat("YYYY-MM-dd HH:mm")).format(getTimestamp());
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
}
