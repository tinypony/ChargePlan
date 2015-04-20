package model.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import model.dataset.BusStop;
import model.planning.solutions.BusCharger;

public class ElectrifiedBusStop extends BusStop {
	
	@Embedded
	private List<BusChargerInstance> chargers;
	
	private Map<String, Integer> chargingTimes;
	
	public ElectrifiedBusStop () {
		chargers = new ArrayList<BusChargerInstance>();
		chargingTimes = new HashMap<String, Integer>();
	}

	public List<BusChargerInstance> getChargers() {
		return chargers;
	}
	
	public void addCharger(BusChargerInstance charger) {
		this.chargers.add(charger);
	}

	public void setChargers(List<BusChargerInstance> chargers) {
		this.chargers = chargers;
	}

	@Override
	@JsonIgnore
	public boolean isFirst() {
		// TODO Auto-generated method stub
		return super.isFirst();
	}

	@Override
	@JsonIgnore
	public boolean isLast() {
		// TODO Auto-generated method stub
		return super.isLast();
	}

	public BusChargerInstance getCharger(String arrival, int chargingTimeSeconds) {
		// TODO Auto-generated method stub
		return this.chargers.get(0);
	}

	public Map<String, Integer> getChargingTimes() {
		return chargingTimes;
	}

	public void setChargingTimes(Map<String, Integer> chargingTimes) {
		this.chargingTimes = chargingTimes;
	}
	
	
}
