package model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

@Entity("routes")
public class BusRoute {
	@Id
	ObjectId id;
	String routeId;
	String description;
	String name;
	
	
	List<BusStop> waypoints = new ArrayList<BusStop>();

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

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

	public List<BusStop> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<BusStop> waypoints) {
		this.waypoints = waypoints;
	}
}
