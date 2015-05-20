package org.emn.calculate.route;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emn.plan.model.BusChargerInstance;
import org.emn.plan.model.ElectrifiedBusStop;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import utils.DateUtils;
import model.dataset.ScheduleStop;
import model.dataset.BusTrip;

public class ChargerEnergyConsumptionModel {

	private ElectrifiedBusStop electrifiedStop;
	private Calendar simDate;
	private List<BusTrip> trips;

	public ChargerEnergyConsumptionModel(ElectrifiedBusStop electrifiedStop,
			Calendar simDate, List<BusTrip> trips) {
		this.electrifiedStop = electrifiedStop;
		this.simDate = simDate;
		this.trips = trips;
	}

	public DailyConsumptionModel getEnergyConsumption() {
		DailyConsumptionModel dcm = new DailyConsumptionModel(simDate);
		this.simDate = DateUtils.rewindCalendar(this.simDate);
		
		this.simDate = DateUtils.rewindCalendar(this.simDate);
		final ElectrifiedBusStop elStop = this.electrifiedStop;
		
		for(BusTrip trip: this.getTrips()) {
			String busRoute = trip.getRouteId();
			
			//Find the scheduled stop that uses the inspected charger
			ScheduleStop sStop = Iterables.find(trip.getStops(), new Predicate<ScheduleStop>() {

				@Override
				public boolean apply(ScheduleStop arg0) {
					return elStop.getStopId().equals(arg0.getStopId());
				}
				
			}, null);
			
			if(sStop == null) {
				continue;
			}
			
			int chargingDuration = this.electrifiedStop.getChargingTime(busRoute);
			this.simDate = DateUtils.arrivalToCalendar(simDate, sStop.getArrival());
			
			//Store consumption info
			if(this.electrifiedStop.getCharger() == null) {
				dcm.consume(trip.getRouteId(), this.simDate, chargingDuration, 0.0);
			} else {
				dcm.consume(trip.getRouteId(), this.simDate, chargingDuration, this.electrifiedStop.getCharger().getType().getPower());
		
			}
		}
		
		return dcm;
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
