package org.emn.calculate.route;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import utils.DateUtils;

public class DailyConsumptionModel {
	
	private Date date;
	private Map<Long, Double> consumptionMap;
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Map<Long, Double> getConsumptionMap() {
		return consumptionMap;
	}
	
	public void setConsumptionMap(Map<Long, Double> consumptionMap) {
		this.consumptionMap = consumptionMap;
	}
	
	/**
	 * Returns the amount of energy consumed in one specified hour in kWh
	 * @param hourOfDay integer from 0 to 23
	 * @return
	 */
	public Double getHourlyEnergy(int hourOfDay) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		DateUtils.rewindCalendar(cal);
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		
		Double hourlyEnergy = 0.0;
		for(int i=0; i<60; i++) {
			Double power = this.getConsumptionMap().get(cal.getTime().getTime());
			hourlyEnergy += this.integrateMinute(power);
		}
		
		return hourlyEnergy;	
	}
	
	/**
	 * Returns amount of kWh consumed in minute with power
	 * @param consumption
	 * @return
	 */
	private Double integrateMinute(Double power) {
		return power/60;
	}
}
