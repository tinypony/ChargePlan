package model.dataset;

import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

@Entity
@Embedded
public class DayStat {

	private String date;
	private int departures;
	private int totalDistance;
	
	@Transient
	private Map<String, Double> emissions;
	
	public DayStat() {
		this.totalDistance = 0;
		this.departures = 0;
		this.emissions = new HashMap<String, Double>();
	}

	public int getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(int totalDistance) {
		this.totalDistance = totalDistance;
	}

	public int getDepartures() {
		return departures;
	}

	public void setDepartures(int departures) {
		this.departures = departures;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public void incrementTotalDistance(int tripDistance) {
		this.totalDistance += tripDistance;
	}
	
	public void incrementDepartures() {
		this.departures++;
	}
	
	public void setEmissions(Map<String, Double>  em) {
		this.emissions = em;
	}
	
	public void setEmissions(String type, Double amount) {
		if(this.emissions == null) {
			this.emissions = new HashMap<String, Double>();
		}
		
		this.emissions.put(type, amount);
	}
	
	public Double getEmissions(String type) {
		if(this.emissions == null) {
			throw new IllegalStateException("Emissions map has not been initialized");
		}
		
		if(!this.emissions.containsKey(type)) {
			return 0.0;
		} else {
			return this.emissions.get(type);
		}
	}
	
	public Map<String, Double> getEmissions() {
		return this.emissions;
	}
}
