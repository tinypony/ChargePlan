package model.dataset;

import java.util.List;

public class DetailedBusRoute extends BusRoute {
	List<BusTrip> trips;
	
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
}
