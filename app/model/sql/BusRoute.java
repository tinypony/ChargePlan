package model.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;


@Entity
public class BusRoute implements Serializable {
	@Id
	String routeId;
	String description;
	String name;
//	
//	@ManyToMany(cascade = {CascadeType.PERSIST})
//	@PrimaryKeyJoinColumn
//	List<BusStop> waypoints = new ArrayList<BusStop>();
	
	@OneToMany(fetch=FetchType.LAZY)
	List<BusTrip> trips = new ArrayList<BusTrip>();
	
	public BusRoute(){}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public List<BusStop> getWaypoints() {
//		return waypoints;
//	}
//
//	public void setWaypoints(List<BusStop> waypoints) {
//		this.waypoints = waypoints;
//	}

	public List<BusTrip> getTrips() {
		return trips;
	}

	public void setTrips(List<BusTrip> trips) {
		this.trips = trips;
	}
	
	
	
	
}
