package dto.message.client;

import java.util.Date;
import java.util.List;

public class SimulationResult {
	
	private String routeId;
	private Date simulationDate;
	private FeasibilitySimulationResult feasibility;
	private List<CostSimulationResult> cost;
	
	public SimulationResult() {
		this.simulationDate = new Date();
	}
	
	public Date getSimulationDate() {
		return simulationDate;
	}
	public void setSimulationDate(Date simulationDate) {
		this.simulationDate = simulationDate;
	}
	public FeasibilitySimulationResult getFeasibility() {
		return feasibility;
	}
	public void setFeasibility(FeasibilitySimulationResult feasibility) {
		this.feasibility = feasibility;
	}
	public List<CostSimulationResult> getCost() {
		return cost;
	}
	public void setCost(List<CostSimulationResult> cost) {
		this.cost = cost;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
}
