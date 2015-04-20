package dto.message.client;

import model.planning.solutions.ElectricBus;

public class SimulationRequest {

	private String routeId;
	private String date;
	private ElectricBus busType;
	
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
	
	
}
