package org.emn.calculate.route;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import utils.DateUtils;

/**
 * Contains minute-by-minute energy consumption power
 * Used to keep track of how much the buses of a particular route drained energy from a specific charger
 * @author tinypony
 *
 */
public class ConsumptionMap {

	private final Long date;
	private Map<Long, List<ChargingEvent>> events;
	
	public ConsumptionMap(Calendar cal) {
		events = new HashMap<Long, List<ChargingEvent>>();
		cal = DateUtils.rewindCalendar(cal);
		this.date = cal.getTime().getTime();
	}
	
	/**
	 * Calculates the average peak power for a given hour
	 * @param hour - hour of the day value from 0 to 23
	 * @return returns average peak power during a given hour in kW
	 */
	public Double getHourlyAveragePower(int hour) {
		Double result = 0.0;
		Calendar cal = DateUtils.getCalendar(this.date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		
		for(int i = 0; i<60; i++) {
			List<ChargingEvent> chEvents = this.events.get(cal.getTime().getTime());
			
			if(chEvents != null) {
				for(ChargingEvent chEvent: chEvents) {
					result += chEvent.getPowerConsumed();
				}
			}
			cal.add(Calendar.MINUTE, 1);
		}
		
		return result;
	}

	public Map<Long, List<ChargingEvent>> getEvents() {
		return events;
	}

	/**
	 * Inserts a charging event at the specified time
	 * @param hour
	 * @param minute
	 * @param duration
	 * @param power
	 */
	public void addChargingEvent(int hour, int minute, long duration, Double power) {
		Calendar cal = DateUtils.getCalendar(this.date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		
		long secondsTillNext = this.secondsTillNextHour(cal);
		long timestamp = cal.getTime().getTime();
		
		if(secondsTillNext < duration) {
			this.addEvent(timestamp, secondsTillNext, power);
			this.addChargingEvent(hour+1, 0, duration - secondsTillNext, power);
		} else {
			this.addEvent(timestamp, duration, power);
		}
	}
	
	private void addEvent(long timestamp, long duration, Double power) {
		ChargingEvent chEvent = new ChargingEvent();
		chEvent.setDuration(duration);
		chEvent.setPower(power);
		
		List<ChargingEvent> chEvents = this.events.get(timestamp);
		if(chEvents == null) {
			chEvents = new ArrayList<ChargingEvent>();
		}
		
		chEvents.add(chEvent);
		this.events.put(timestamp, chEvents);
	}
	
	private long secondsTillNextHour(Calendar cal) {
		Calendar endingCal = Calendar.getInstance();
		endingCal.setTime(cal.getTime());
		endingCal.add(Calendar.HOUR_OF_DAY, 1);
		endingCal.set(Calendar.MINUTE, 0);
		return DateUtils.getDateDiff(cal.getTime(), endingCal.getTime(), TimeUnit.SECONDS);
	}
	
}
