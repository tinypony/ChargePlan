package org.emn.calculate;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;

public class EnergyPricingModel {
	


	public Double getEnergyCost(Map<Long, Double> consumptionMap) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(this.getFirstTimestamp(consumptionMap.entrySet())));
		
		for(int i=0; i<consumptionMap.entrySet().size();i++ ) {
			Double minuteConsumption = consumptionMap.get(cal.getTime().getTime());
			double energyConsumed = minuteConsumption * (1/60); //= x kWh
		}
		
		return 42.0;
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
