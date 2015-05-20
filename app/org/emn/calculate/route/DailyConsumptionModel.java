package org.emn.calculate.route;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.DateUtils;

/**
 * Aggregates power usage from all electrified routes passing through a specific bus stop
 * and provides detailed information about power distribution
 * @author tinypony
 *
 */
public class DailyConsumptionModel {
	
	private Long date;
	private Map<String, ConsumptionMap> routeConsumptionMaps;
	
	public DailyConsumptionModel(Calendar date) {
		this.date = date.getTime().getTime();
		routeConsumptionMaps = new HashMap<String, ConsumptionMap>();
	}
	
	
	public Map<Integer, List<HourlyConsumptionEntry>> getHourlyConsumptionDistribution() {
		Map<Integer, List<HourlyConsumptionEntry>> hourlyDistribution 
			= new HashMap<Integer, List<HourlyConsumptionEntry>>();
		
		for(int i=0; i<24; i++) {
			hourlyDistribution.put(i, this.getHourlyEnergy(i));
		}
		
		return hourlyDistribution;
	}
	
	/**
	 * Returns the amount of energy consumed in one specified hour in kWh
	 * @param hourOfDay integer from 0 to 23
	 * @return
	 */
	public List<HourlyConsumptionEntry> getHourlyEnergy(int hourOfDay) {
		List<HourlyConsumptionEntry> result = new ArrayList<HourlyConsumptionEntry>();
		
		for(String routeId: this.routeConsumptionMaps.keySet()) {
			result.add(this.getHourlyEnergy(hourOfDay, routeId));
		}
		
		return result;
	}
	
	/**
	 * Returns the amount of energy consumed in one specified hour in kWh
	 * @param hourOfDay integer from 0 to 23
	 * @return
	 */
	public HourlyConsumptionEntry getHourlyEnergy(int hourOfDay, String routeId) {
		ConsumptionMap routeConsumptionMap = routeConsumptionMaps.get(routeId);
		Double averagePower = routeConsumptionMap.getHourlyAveragePower(hourOfDay);
		HourlyConsumptionEntry newEntry = new HourlyConsumptionEntry();
		newEntry.setRouteId(routeId);
		newEntry.setAvgPower(averagePower);
		newEntry.setTotalEnergy(averagePower);
		return newEntry;
	}


	/**
	 * Inserts consumption record
	 * @param routeId
	 * @param simDate
	 * @param chargingDuration
	 * @param power
	 */
	
	public void consume(String routeId, Calendar simDate, int duration,
			double power) {
		System.out.println("Consume route:"+routeId+", duration:"+duration);
		ConsumptionMap map = this.routeConsumptionMaps.get(routeId);
		
		if(map == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(simDate.getTime());
			map = new ConsumptionMap(cal);
		}
		
		map.addChargingEvent(simDate.get(Calendar.HOUR_OF_DAY), simDate.get(Calendar.MINUTE), duration, power);
		this.routeConsumptionMaps.put(routeId, map);
	}


	public Date getDate() {
		return new Date(this.date);
	}
	
	public Double getHourlyPower(int hour) {
		Double result = 0.0;
		List<HourlyConsumptionEntry> entries = this.getHourlyEnergy(hour);
		
		for(HourlyConsumptionEntry e: entries) {
			result += e.getAvgPower();
		}
		
		return result;
	}
	
	public List<HourlyConsumptionEntry> getDailyPeakPowerContributors() {
		Double result = 0.0;
		List<HourlyConsumptionEntry> contributions = new ArrayList<HourlyConsumptionEntry>();
		
		for(int i=0; i<24; i++) {
			Double hourlyPower = this.getHourlyPower(i);
			if(result < hourlyPower) {
				result = hourlyPower;
				contributions = this.getHourlyEnergy(i);
			}
		}
		
		return contributions;
	}


	public Double getDailyPeakPower() {
		Double result = 0.0;
		
		for(int i=0; i<24; i++) {
			Double hourlyPower = this.getHourlyPower(i);
			if(result < hourlyPower) {
				result = hourlyPower;
			}
		}
		
		return result;
	}
}
