package org.emn.calculate.price;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.emn.calculate.route.DailyConsumptionModel;
import org.emn.calculate.route.HourlyConsumptionEntry;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import scala.Array;
import utils.DateUtils;


public class EnergyPricingModel {
	
	private IEnergyPriceProvider priceProvider;
	private static final Double FLAT_MONTHLY_FEE = 340.0;
	private static final Double SEASON_PEAK_FEE = 125.0;
	private static final Double SEASON_TRANSFER_FEE = (5.7/100); //per kWh
	
	public EnergyPricingModel(IEnergyPriceProvider provider) {
		this.priceProvider = provider;
	}
	
	/**
	 * Returns total amount of energy cost
	 * @param consumptionHistory
	 * @return
	 */
	public Double getEnergyCost(List<DailyConsumptionModel> consumptionHistory, String routeId) {
		Calendar cal = Calendar.getInstance();
		Double totalEnergyCost = 0.0;
		List<HourlyConsumptionEntry> tmp;
		
		for(DailyConsumptionModel model: consumptionHistory) {
			cal.setTime(model.getDate());
			DateUtils.rewindCalendar(cal);
			
			//get cost for every hour
			for(int i=0; i<24; i++) {
				tmp = model.getHourlyEnergy(i);
				Double totalHourEnergy = this.getTotalEnergy(tmp, routeId);
				cal.set(Calendar.HOUR_OF_DAY, i);
				totalEnergyCost += this.getHourlyProductionCost(cal.getTime(), totalHourEnergy) 
						+ this.getHourlyTransmissionCost(cal.getTime(), totalHourEnergy);
				
			}
		}
		
		int days = consumptionHistory.size();
		totalEnergyCost += FLAT_MONTHLY_FEE * ( days / 31.0);
		totalEnergyCost += this.getPeakPowerCost(consumptionHistory, routeId);
		return totalEnergyCost;
	}
	
	public Double getPeakPowerCost(List<DailyConsumptionModel> consumptionHistory, String routeId) {
		int days = consumptionHistory.size();
		Double peak = 0.0;
		
		if(routeId == null) {
			peak = this.getHistoryPeakPower(consumptionHistory);
			return SEASON_PEAK_FEE * peak * (days / 31.0);
		} else {
			Double tmp = 0.0;
			List<HourlyConsumptionEntry> contributors = new ArrayList<HourlyConsumptionEntry>();
			
			for(DailyConsumptionModel m: consumptionHistory) {  
				List<HourlyConsumptionEntry> test = m.getDailyPeakPowerContributors();
				Double totalPower = this.getTotalPower(test);
				
				if(totalPower > tmp) {
					tmp = totalPower;
					contributors = test;
				}
			}
			
			Double myPortion = this.getPeakPowerContribution(contributors, routeId);
			Double totalCost = SEASON_PEAK_FEE * tmp * (days / 31.0);
			return totalCost * myPortion;
		}
		
		
	}
	
	/**
	 * Returns highest hourly peak consumption from history
	 * @param history
	 * @return
	 */
	public Double getHistoryPeakPower(List<DailyConsumptionModel> history) {
		Double result = 0.0;
		
		for(DailyConsumptionModel m: history) {
			Double dailyPeak = m.getDailyPeakPower();
			if(dailyPeak > result) {
				result = dailyPeak;
			}
		}
		return result;
	}
	
	public List<HourlyConsumptionEntry> getHistoryPeakPowerContributors(List<DailyConsumptionModel> history) {
		List<HourlyConsumptionEntry> contributors = new ArrayList<HourlyConsumptionEntry>();
		Double tmp = 0.0;
		
		for(DailyConsumptionModel m: history) {
			List<HourlyConsumptionEntry> candidate = m.getDailyPeakPowerContributors();
			Double peakPower = this.getTotalPower(candidate);
			
			if(peakPower > tmp) {
				tmp = peakPower;
				contributors = candidate;
			}
		}
		
		return contributors;
	}
	
	public Double getPeakPowerContribution(List<HourlyConsumptionEntry> peakPowerContributions, final String routeId) {
		Double totalPower = this.getTotalPower(peakPowerContributions);
		List<HourlyConsumptionEntry> myContributions 
			= Lists.newArrayList(Iterables.filter(peakPowerContributions, new Predicate<HourlyConsumptionEntry>() {

			@Override
			public boolean apply(HourlyConsumptionEntry arg0) {
				return routeId.equals(arg0.getRouteId());
			}
		}));
		Double myPower = this.getTotalPower(myContributions);
		return myPower / totalPower;
	}
	
	/**
	 * Calculates total energy consumed
	 * @param hourEnergy
	 * @return
	 */
	public Double getTotalEnergy(List<HourlyConsumptionEntry> hourEnergy) {
		Double result = 0.0;
		
		for(HourlyConsumptionEntry en: hourEnergy) {
			result += en.getTotalEnergy();
		}
		
		return result;
	}
	
	public Double getTotalEnergy(List<HourlyConsumptionEntry> hourEnergy, String routeId) {
		if(routeId == null) {
			return this.getTotalEnergy(hourEnergy);
		}
		
		Double result = 0.0;
		for(HourlyConsumptionEntry en: hourEnergy) {
			if(routeId.equals(en.getRouteId())) {
				result += en.getTotalEnergy();
			}
		}
		
		return result;
	}
	
	public Double getTotalPower(List<HourlyConsumptionEntry> hourEnergy) {
		Double result = 0.0;
		
		for(HourlyConsumptionEntry en: hourEnergy) {		
			result += en.getAvgPower();
		}
		
		return result;
	}
	
	/**
	 * Returns cost of consumed energy within specified hour
	 * @param time the beginning of an hour on a specific day
	 * @param energyConsumed the amount of energy consumed during the hour in kWh
	 * @return the price of consumed energy
	 */
	public Double getHourlyProductionCost(Date time, Double energyConsumed) {
		Double price = this.priceProvider.getMWhPrice(time);
		return price * energyConsumed / 1000;
	}
	
	/**
	 * Get cost of transmitting specified amount of energy 
	 * @param time
	 * @param energyConsumed
	 * @return
	 */
	public Double getHourlyTransmissionCost(Date time, Double energyConsumed) {
		return SEASON_TRANSFER_FEE * energyConsumed;
	}
}
