package org.emn.calculate.route;

import java.util.Date;

public class ChargingEvent {

	private Date start;
	private long duration; //in seconds
	private Double power; //in kW
	
	/**
	 * Returns amount of power consumed in kWh
	 * @return
	 */
	public Double getPowerConsumed() {
		return power * (duration/ (60.0*60.0));
	}
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public Double getPower() {
		return power;
	}
	public void setPower(Double power) {
		this.power = power;
	}
}
