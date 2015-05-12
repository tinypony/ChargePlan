package model.dataset;

import java.util.List;

import dto.message.client.SimulationResult;

public class DetailedBusRoute extends BusRoute {
	private List<BusTrip> trips;
	private SimulationResult latestSimulation;
	
	public DetailedBusRoute(BusRoute inst) {
		this.setId(inst.getId());
		this.setRouteId(inst.getRouteId());
		this.setName(inst.getName());
		this.setLongName(inst.getLongName());
		this.setStats(inst.getStats());
	}

	public List<BusTrip> getTrips() {
		return trips;
	}

	public void setTrips(List<BusTrip> trips) {
		this.trips = trips;
	}

	public SimulationResult getLatestSimulation() {
		return latestSimulation;
	}

	public void setLatestSimulation(SimulationResult latestSimulation) {
		this.latestSimulation = latestSimulation;
	}
}
