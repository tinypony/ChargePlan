package dto.message.client;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.emn.plan.model.ElectricBus;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("simulations")
public class SimulationResult {
	
	@Id
	private String routeId;
	private Date simulationDate;
	private FeasibilitySimulationResult feasibility;
	private List<CostSimulationResult> cost;
	private ElectricBus type;
	
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

	public ElectricBus getType() {
		return type;
	}

	public void setType(ElectricBus type) {
		this.type = type;
	}
}
