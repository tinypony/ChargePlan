package model.planning;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import model.dataset.BusStop;
import model.planning.solutions.BusCharger;

public class ElectrifiedBusStop extends BusStop {
	
	@Embedded
	private List<BusChargerInstance> chargers;
	
	public ElectrifiedBusStop () {
		chargers = new ArrayList<BusChargerInstance>();
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
	
	
}
