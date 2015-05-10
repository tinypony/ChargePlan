package dto.message.client;

import java.util.Date;

public class SimulationResult {
	
	private Date simulationDate;
	private FeasibilitySimulationResult feasibility;
	private CostSimulationResult cost;
	
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
	public CostSimulationResult getCost() {
		return cost;
	}
	public void setCost(CostSimulationResult cost) {
		this.cost = cost;
	}
	
	
	

}
