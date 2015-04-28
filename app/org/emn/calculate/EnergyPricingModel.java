package org.emn.calculate;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.emn.calculate.route.DailyConsumptionModel;

import utils.DateUtils;

import com.google.common.collect.Lists;

public class EnergyPricingModel {
	
	private IEnergyPriceProvider priceProvider;
	private static final Double FLAT_MONTHLY_FEE = 340.0;
	private static final Double SEASON_PEAK_FEE = 125.0;
	private static final Double SEASON_TRANSFER_FEE = (5.7/100); //per kWh
	
	public EnergyPricingModel(IEnergyPriceProvider provider) {
		this.priceProvider = provider;
	}
	
	public Double getEnergyCost(List<DailyConsumptionModel> consumptionHistory) {
		Calendar cal = Calendar.getInstance();
		Double totalEnergy = 0.0;
		Double totalEnergyCost = 0.0;
		Double tmp;
		
		for(DailyConsumptionModel model: consumptionHistory) {
			cal.setTime(model.getDate());
			DateUtils.rewindCalendar(cal);
			
			//get cost for every hour
			for(int i=0; i<24; i++) {
				tmp = model.getHourlyEnergy(i);
				totalEnergy += tmp;
				cal.set(Calendar.HOUR_OF_DAY, i);
				totalEnergyCost += this.getHourlyProductionCost(cal.getTime(), tmp) + this.getHourlyTransmissionCost(cal.getTime(), tmp);
			}
		}
		
		int days = consumptionHistory.size();
		totalEnergyCost += FLAT_MONTHLY_FEE * ( days / 31.0);
		totalEnergyCost += SEASON_PEAK_FEE * this.getHighestPeak(consumptionHistory) * (days / 31.0);
		
		return totalEnergyCost;
	}
	
	protected Double getHighestPeak(List<DailyConsumptionModel> powerHistory) {
		Double result = 0.0;
		
		for(DailyConsumptionModel m: powerHistory) {
			for(Entry<Long, Double> entry : m.getConsumptionMap().entrySet()) {
				if(entry.getValue() > result) {
					result = entry.getValue();
				}
			}
		}
		
		return result;		
	}
	
	/**
	 * Returns cost of consumed energy within specified hour
	 * @param time the beginning of an hour on a specific day
	 * @param energyConsumed the amount of energy consumed during the hour in kWh
	 * @return the price of consumed energy
	 */
	protected Double getHourlyProductionCost(Date time, Double energyConsumed) {
		Double price = this.priceProvider.getMWhPrice(time);
		return price * energyConsumed / 1000;
	}
	
	
	protected Double getHourlyTransmissionCost(Date time, Double energyConsumed) {
		return SEASON_TRANSFER_FEE * energyConsumed;
	}

	private Long getFirstTimestamp(Set<Entry<Long, Double>> entries) {
		List<Entry<Long, Double>> entryList = Lists.newArrayList(entries);
		
		Collections.sort(entryList, new Comparator<Entry<Long, Double>>() {
			@Override
			public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		return entryList.get(0).getKey();
	}
}
