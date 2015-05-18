package org.emn.calculate.bus;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.emn.plan.model.ElectricBus;

public class DumbConsumptionProfile implements IConsumptionProfile {

	private static final double RUSH_HOUR_MULTIPLIER = 1.2;
	private static final double WINTER_SEASON_MULTIPLIER = 2;
	
	private double consumption;
	
	
	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	@Override
	public double getConsumption(ElectricBus bus, Map<String, Object> params) {
		Date date = (Date) params.get("date");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		double mult = 1;
		int month = cal.get(Calendar.MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		if(month < 4 || month > 9) {
			mult *= WINTER_SEASON_MULTIPLIER;
		}
		
		if( (hour > 7 && hour < 11) || (hour > 15 && hour < 18)) {
			mult *= RUSH_HOUR_MULTIPLIER;
		}
		
		return this.getConsumption() * mult;
	}

}
