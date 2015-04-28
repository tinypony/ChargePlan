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
import model.planning.BusChargerInstance;
import model.planning.ElectrifiedBusStop;

public class ChargerEnergyConsumptionModel {

	private ElectrifiedBusStop electrifiedStop;
	private Calendar simDate;
	private List<BusTrip> trips;

	public ChargerEnergyConsumptionModel(ElectrifiedBusStop electrifiedStop,
			Calendar simDate, List<BusTrip> trips) {
		this.electrifiedStop = electrifiedStop;
		this.simDate = simDate;
		this.trips = trips;
		System.out.println(trips.size()+"");
	}

	public Map<Long, Double> getEnergyConsumption() {
		Map<Long, Double> energyProfile = new HashMap<Long, Double>();
		this.simDate = DateUtils.rewindCalendar(this.simDate);

		
		for(int i=0; i < 60 * 24-1; i++ ) {
			energyProfile.put(this.simDate.getTime().getTime(), 0.0);
			this.simDate.add(Calendar.MINUTE, 1);
		}
		this.simDate = DateUtils.rewindCalendar(this.simDate);
		final ElectrifiedBusStop elStop = this.electrifiedStop;
		
		for(BusTrip trip: this.getTrips()) {
			String busRoute = trip.getRouteId();
			
			ScheduleStop sStop = Iterables.find(trip.getStops(), new Predicate<ScheduleStop>() {

				@Override
				public boolean apply(ScheduleStop arg0) {
					return elStop.getStopId().equals(arg0.getStopId());
				}
				
			}, null);
			
			if(sStop == null || this.electrifiedStop.getCharger() == null) {
				continue;
			}
			
			int chargingTime = this.electrifiedStop.getChargingTime(busRoute);
			this.simDate = DateUtils.stringToCalendar(simDate, sStop.getArrival());
			energyProfile = this.consume(energyProfile, this.simDate, chargingTime, this.electrifiedStop.getCharger());
		}
		
		return energyProfile;
	}

	/**
	 * Updates map with power starting from start and lasting for chargingTime
	 * seconds
	 * 
	 * @param map
	 *            contains consumption with minute precision
	 * @param start
	 *            starting time of energy consumption period
	 * @param chargingTime
	 *            amount of seconds spent consuming energy using charger
	 * @param charger
	 *            bus charger
	 * @return
	 */
	private Map<Long, Double> consume(Map<Long, Double> map, Calendar start,
			int chargingTime, BusChargerInstance charger) {
		
		int minutes;
		
		if(chargingTime%60 == 0) {
			minutes = (chargingTime / 60);
		} else {
			minutes = (chargingTime / 60 ) + 1;
		}
		
		
		System.out.println(minutes);
		for (int i = 0; i < minutes; i++) {
			Double consumptionKW = map.get(start.getTime().getTime());
			if(consumptionKW == null) {
				continue;
			}
			
			consumptionKW += charger.getType().getPower();
			map.put(start.getTime().getTime(), consumptionKW);
			start.add(Calendar.MINUTE, 1);
		}

		return map;
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
