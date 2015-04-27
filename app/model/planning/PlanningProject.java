package model.planning;

import java.util.ArrayList;
import java.util.List;

import model.dataset.aggregation.BusRouteAggregation;
import model.dataset.aggregation.BusRouteAggregationLight;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import serialization.ObjectIdSerializer;
import akka.util.Collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


@Entity("projects")
public class PlanningProject {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	ObjectId id;
	String name;
	String location;
	
	@Embedded
	private List<BusRouteAggregationLight> routes;
	
	@Embedded
	private List<ElectrifiedBusStop> stops;
	
	public PlanningProject() {
		this.routes = new ArrayList<BusRouteAggregationLight>();
		this.stops = new ArrayList<ElectrifiedBusStop>();
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
	
	public List<ElectrifiedBusStop> getStops() {
		return stops;
	}

	public void setStops(List<ElectrifiedBusStop> stops) {
		this.stops = stops;
	}
	
	public void addStop(ElectrifiedBusStop stop) {
		this.stops.add(stop);
	}

	@JsonIgnore
	public ElectrifiedBusStop getElectrifiedStop(String stopId) {
		ElectrifiedBusStop stop = null;
		for(ElectrifiedBusStop s: this.stops) {
			if(s.getStopId().equals(stopId)) {
				stop = s;
				break;
			}
		}
		return stop;
	}
	
	@JsonIgnore
	public BusRouteAggregationLight getBusRoute(final String routeId) {
		return Iterables.find(this.getRoutes(), new Predicate<BusRouteAggregationLight>() {

			@Override
			public boolean apply(BusRouteAggregationLight arg0) {
				return routeId.equals(arg0.getRouteId());
			}
			
		}, null);
	}
}
