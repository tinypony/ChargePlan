package model.planning;

import java.util.ArrayList;
import java.util.List;

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
	List<IncludedBusRoute> routes;
	
	public PlanningProject() {
		this.routes = new ArrayList<IncludedBusRoute>();
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

	public List<IncludedBusRoute> getRoutes() {
		return routes;
	}

	public void setRoutes(List<IncludedBusRoute> routes) {
		this.routes = routes;
	}
}
