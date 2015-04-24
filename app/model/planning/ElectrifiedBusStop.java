package model.planning;

import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnore;

import model.dataset.BusStop;

public class ElectrifiedBusStop extends BusStop {
	
	@Embedded
	private BusChargerInstance charger;
	
	private Map<String, Integer> chargingTimes; // tuple (routeId, charging time in seconds) 
	
	public ElectrifiedBusStop () {
//		charger = new ArrayList<BusChargerInstance>();
		chargingTimes = new HashMap<String, Integer>();
	}

//	public List<BusChargerInstance> getChargers() {
//		return chargers;
//	}
//	
//	public void addCharger(BusChargerInstance charger) {
//		this.chargers.add(charger);
//	}
//
//	public void setChargers(List<BusChargerInstance> chargers) {
//		this.chargers = chargers;
//	}

	@Override
	@JsonIgnore
	public boolean isFirst() {
		return super.isFirst();
	}

	@Override
	@JsonIgnore
	public boolean isLast() {
		return super.isLast();
	}
	
	public BusChargerInstance getCharger() {
		return this.charger;
	}

	public BusChargerInstance getCharger(String arrival, int chargingTimeSeconds) {
				return this.charger;
	}
	
	public void setCharger(BusChargerInstance c) {
		this.charger = c;
	}

	public Map<String, Integer> getChargingTimes() {
		return chargingTimes;
	}
		
	public Integer getChargingTime(String routeId) {
		return chargingTimes.get(routeId);
	}

	public void setChargingTimes(Map<String, Integer> chargingTimes) {
		this.chargingTimes = chargingTimes;
	}
}
