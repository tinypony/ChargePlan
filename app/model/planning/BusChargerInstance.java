package model.planning;

import org.mongodb.morphia.annotations.Embedded;

import model.planning.solutions.BusCharger;

@Embedded
public class BusChargerInstance {

	@Embedded
	private BusCharger type;
	private String chargingSchedule;
	

	public BusCharger getType() {
		return type;
	}

	public void setType(BusCharger type) {
		this.type = type;
	}

	public String getChargingSchedule() {
		return chargingSchedule;
	}

	public void setChargingSchedule(String chargingSchedule) {
		this.chargingSchedule = chargingSchedule;
	}
}
