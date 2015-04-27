package model.dataset.aggregation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BusRouteAggregationLight {

	public enum State {
		UNSIMULATED,
		SIMULATED_OK,
		SIMULATED_WARNING,
		SIMULATED_FAIL,
		RESIMULATE;
	}
	
	private String routeId;
	private String longName;
	private String name;
	private State state;
	
	public BusRouteAggregationLight() {
		this.state = State.UNSIMULATED;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}
