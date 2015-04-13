package model.planning;

import java.util.List;

import model.dataset.BusRoute;
import model.dataset.BusTrip;

public class DetailedBusRoute extends BusRoute {
	List<BusTrip> trips;
	
	public DetailedBusRoute(BusRoute inst) {
		this.setId(inst.getId());
		this.setRouteId(inst.getRouteId());
		this.setName(inst.getName());
		this.setStats(inst.getStats());
	}

	public List<BusTrip> getTrips() {
		return trips;
	}

	public void setTrips(List<BusTrip> trips) {
		this.trips = trips;
	}
}
