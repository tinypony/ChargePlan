package org.emn.calculate.route;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import utils.DateUtils;
import model.dataset.ScheduleStop;
import model.dataset.BusTrip;
import model.planning.ElectrifiedBusStop;

public class RouteEnergyConsumptionModel {

	private List<ElectrifiedBusStop> electrifiedStops;
	private Calendar simDate;
	private List<BusTrip> trips;
	
	public RouteEnergyConsumptionModel(List<ElectrifiedBusStop> electrifiedStops, 
			Calendar simDate, List<BusTrip> trips) {
		this.electrifiedStops = electrifiedStops;
		this.simDate = simDate;
		this.trips = trips;
	}
	
	public Map<Date, Double> getEnergyConsumption() {
		HashMap<Date, Double> energyProfile = new HashMap<Date, Double>();
		this.simDate = DateUtils.rewindCalendar(this.simDate);
		
		for(int i=0; i < 60 * 24; i++ ) {
			energyProfile.put(this.simDate.getTime(), 0.0);
			this.simDate.add(Calendar.MINUTE, 1);
		}
		
		for(BusTrip trip: this.getTrips()) {
			for(final ScheduleStop stop : trip.getStops()) {
				ElectrifiedBusStop elStop = Iterables.find(this.electrifiedStops, new Predicate<ElectrifiedBusStop>() {
					@Override
					public boolean apply(ElectrifiedBusStop arg0) {
						return stop.getStopId().equals(arg0.getStopId());
					}
				}, null);
				
				if(elStop!=null) {
					
				}
			}
		}
		
		return energyProfile;
	}

	public List<ElectrifiedBusStop> getElectrifiedStops() {
		return electrifiedStops;
	}

	public void setElectrifiedStops(List<ElectrifiedBusStop> electrifiedStops) {
		this.electrifiedStops = electrifiedStops;
	}

	public Calendar getSimDate() {
		return simDate;
	}

	public void setSimDate(Calendar simDate) {
		this.simDate = simDate;
	}

	public List<BusTrip> getTrips() {
		return trips;
	}

	public void setTrips(List<BusTrip> trips) {
		this.trips = trips;
	}
}
