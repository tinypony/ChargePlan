package model.dataset.aggregation;

import java.util.Map;

import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import model.dataset.DayStat;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BusRouteAggregation extends BusRouteAggregationLight {
	private int count;
	private long totalDriven;
	private long totalLength;
	private int minDepartures;
	private int maxDepartures;
	
	private Map<String, Double> emissions;
	
	public BusRouteAggregation() {
		this.minDepartures = Integer.MAX_VALUE;
		this.maxDepartures = Integer.MIN_VALUE;
		this.totalDriven = 0L;
		count = 0;
	}
	
	public void addStat(DayStat st) {
		count++;
		if(this.maxDepartures < st.getDepartures()) {
			this.maxDepartures = st.getDepartures();
		}
		
		if(this.minDepartures > st.getDepartures()) {
			this.minDepartures = st.getDepartures();
		}
		
		this.totalDriven += st.getTotalDistance();
		this.totalLength += st.getLength();
	}

	
	public long getTotalDriven() {
		return this.totalDriven /  this.count;
	}
	
	public int getMinDepartures() {
		return minDepartures;
	}
	
	public int getMaxDepartures() {
		return maxDepartures;
	}
	
	public long getLength() {
		return this.totalLength / this.count;
	}

	public Map<String, Double> getEmissions() {
		return emissions;
	}

	public void setEmissions(Map<String, Double> emissions) {
		this.emissions = emissions;
	}



	
}
