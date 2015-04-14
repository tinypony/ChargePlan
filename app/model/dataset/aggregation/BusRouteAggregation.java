package model.dataset.aggregation;

import java.util.List;
import java.util.Set;

public class BusRouteAggregation {
	String name;
	List<Integer> totalDrivenList;
	List<Integer> departuresList;
	private Set<String> dates;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getTotalDrivenList() {
		return totalDrivenList;
	}

	public void setTotalDrivenList(List<Integer> totalDrivenList) {
		this.totalDrivenList = totalDrivenList;
	}

	public List<Integer> getDeparturesList() {
		return departuresList;
	}

	public void setDeparturesList(List<Integer> departuresList) {
		this.departuresList = departuresList;
	}
	
	

}
