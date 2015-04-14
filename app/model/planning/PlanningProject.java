package model.planning;

import java.util.ArrayList;
import java.util.List;

import model.dataset.aggregation.BusRouteAggregation;
import model.dataset.aggregation.BusRouteAggregationLight;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("projects")
public class PlanningProject {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	ObjectId id;
	String name;
	String location;
	List<BusRouteAggregationLight> routes;
	
	public PlanningProject() {
		this.routes = new ArrayList<BusRouteAggregationLight>();
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<BusRouteAggregationLight> getRoutes() {
		return routes;
	}

	public void setRoutes(List<BusRouteAggregationLight> routes) {
		this.routes = routes;
	}
}
