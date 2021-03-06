package model.dataset;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("routes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusRoute {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	ObjectId id;
	String routeId;
	String description;
	String name;
	String longName;
	
	@Embedded
	List<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	@Embedded
	List<DayStat> stats = new ArrayList<DayStat>();

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

	public List<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public List<DayStat> getStats() {
		return stats;
	}

	public void setStats(List<DayStat> stats) {
		this.stats = stats;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}
	
	
}
