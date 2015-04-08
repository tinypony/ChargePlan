package model.planning;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import model.dataset.BusRoute;

@JsonIgnoreProperties(ignoreUnknown=true)
public class IncludedBusRoute {
	String name;
	List<BusRoute> instances;
	Map<String, String> simParameters;
	Map<String, String> simResults;

	public IncludedBusRoute() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<BusRoute> getInstances() {
		return this.instances;
	}

	public void setInstances(List<BusRoute> routes) {
		this.instances = routes;
	}

	public Map<String, String> getSimParameters() {
		return simParameters;
	}

	public void setSimParameters(Map<String, String> simParameters) {
		this.simParameters = simParameters;
	}

	public Map<String, String> getSimResults() {
		return simResults;
	}

	public void setSimResults(Map<String, String> simResults) {
		this.simResults = simResults;
	}

}
