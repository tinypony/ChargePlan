package org.emn.calculate.route;

public class HourlyConsumptionEntry {

	private String routeId;
	private Double totalEnergy;
	private Double avgPower;
	
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public Double getTotalEnergy() {
		return totalEnergy;
	}
	public void setTotalEnergy(Double totalEnergy) {
		this.totalEnergy = totalEnergy;
	}
	public Double getAvgPower() {
		return avgPower;
	}
	public void setAvgPower(Double avgPower) {
		this.avgPower = avgPower;
	}
	
	
}
