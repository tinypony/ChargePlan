package dto.message.client;

import org.emn.plan.model.ElectricBus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SimulationRequest {

	private String routeId;
	private String date;
	private ElectricBus busType;
	private int minWaitingTime;
	
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public ElectricBus getBusType() {
		return busType;
	}
	public void setBusType(ElectricBus busType) {
		this.busType = busType;
	}
	public int getMinWaitingTime() {
		return this.minWaitingTime;
	}
	public void setMinWaitingTime(int val) {
		this.minWaitingTime = val;
	}
	
}
