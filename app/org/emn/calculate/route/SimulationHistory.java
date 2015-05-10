package org.emn.calculate.route;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.types.ObjectId;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dto.message.client.CostSimulationResult;
import dto.message.client.FeasibilitySimulationResult;

public class SimulationHistory {
	
	@JsonSerialize(using=ObjectIdSerializer.class)
	private ObjectId id;
	private String routeId;
	private Queue<FeasibilitySimulationResult> feasibilityResults;
	private Queue<CostSimulationResult> costResults;
	
	public SimulationHistory() {
		feasibilityResults = new LinkedBlockingQueue<FeasibilitySimulationResult>();
		costResults = new LinkedBlockingQueue<CostSimulationResult>();
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public Queue<FeasibilitySimulationResult> getFeasibilityResults() {
		return feasibilityResults;
	}

	public void setFeasibilityResults(
			Queue<FeasibilitySimulationResult> feasibilityResults) {
		this.feasibilityResults = feasibilityResults;
	}

	public Queue<CostSimulationResult> getCostResults() {
		return costResults;
	}

	public void setCostResults(Queue<CostSimulationResult> costResults) {
		this.costResults = costResults;
	}
	
	
}
