package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.dataset.BusStop;
import model.dataset.BusTrip;
import model.dataset.RouteDirection;
import model.dataset.aggregation.BusRouteAggregationLight;

import org.bson.types.ObjectId;
import org.emn.plan.model.BusCharger;
import org.emn.plan.model.BusChargerInstance;
import org.emn.plan.model.ElectrifiedBusStop;
import org.emn.plan.model.PlanningProject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import controllers.solutions.ChargerController;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.MongoUtils;

public class ProjectController extends Controller {
	
	public static Result getProjects() {
		Datastore ds = MongoUtils.ds();
		List<PlanningProject> projects = ds.createQuery(PlanningProject.class).asList();
		return ok((new ObjectMapper()).valueToTree(projects)).as("application/json");
	}
	
	public static Result getProject(String projectId) {
		return ok((new ObjectMapper()).valueToTree(getProjectObject(projectId))).as("application/json");
	}
	
	public static PlanningProject getProjectObject(String projectId) {
		Datastore ds = MongoUtils.ds();
		ObjectId id = new ObjectId(projectId);
		
		Query<PlanningProject> q = ds.createQuery(PlanningProject.class);
		q.field("id").equals(id);
		
		return q.get();
	}
	
	public static Result createProject() {
		Datastore ds = MongoUtils.ds();
		PlanningProject proj = new PlanningProject();
		ds.save(proj);
		
		return created((new ObjectMapper()).valueToTree(proj)).as("application/json");
	}
	
	public static PlanningProject addCharger(PlanningProject project, String stopId, String chargerId, String routeId, Integer minChargingTime) {
		ElectrifiedBusStop stop = project.getElectrifiedStop(stopId);
		
		if(stop == null) {
			stop = new ElectrifiedBusStop();
			BusStop plainStop = StopsController.getStopModel(stopId);
			stop.setStopId(plainStop.getStopId());
			stop.setName(plainStop.getName());
			stop.setX(plainStop.getX());
			stop.setY(plainStop.getY());
			project.addStop(stop);
		}
		
		BusCharger chargerType = ChargerController.getChargerModel(chargerId);
		BusChargerInstance chargerInstance = new BusChargerInstance();
		chargerInstance.setType(chargerType);
		stop.setCharger(chargerInstance);
		stop.getChargingTimes().put(routeId, minChargingTime);
		return project;
	}
	
	public static Result updateStop(String projectId) throws JsonProcessingException {
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		JsonNode bodyJson = request().body().asJson();
		String routeId = bodyJson.get("route").asText();
		String stopId = bodyJson.get("stop").asText();
		String charger = om.treeToValue(bodyJson.get("chargersToAdd"), String.class);
		int minChargingTime = bodyJson.get("minChargingTime").asInt();		
		
		ObjectId id = new ObjectId(projectId);
		
		Query<PlanningProject> q = ds.createQuery(PlanningProject.class);
		q.field("id").equals(id);
		PlanningProject project = q.get();
		
		ElectrifiedBusStop stop = project.getElectrifiedStop(stopId);
		
		if(stop == null) {
			stop = new ElectrifiedBusStop();
			BusStop plainStop = StopsController.getStopModel(stopId);
			stop.setStopId(plainStop.getStopId());
			stop.setName(plainStop.getName());
			stop.setX(plainStop.getX());
			stop.setY(plainStop.getY());
			project.addStop(stop);
		}
		
		if(!"-1".equals(charger)) {
			stop.setCharger(getChargerInstance(charger));
			stop.getChargingTimes().put(routeId, minChargingTime);
		} else {
			stop.setCharger(null);
		}
		
		ds.save(project);
		return ok();
	}
	
	public static Result addChargers(String projectId) throws JsonProcessingException {
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		JsonNode bodyJson = request().body().asJson();
		String charger = om.treeToValue(bodyJson.get("charger"), String.class);
		Integer minChargingTime = om.treeToValue(bodyJson.get("minChargingTime"), Integer.class);
		ObjectId id = new ObjectId(projectId);
		
		Query<PlanningProject> q = ds.createQuery(PlanningProject.class);
		q.field("id").equals(id);
		PlanningProject project = q.get();
		
		List<BusRouteAggregationLight> routes = project.getRoutes();
		List<String> routeNames = Lists.newArrayList(Iterables.transform(routes, new Function<BusRouteAggregationLight, String>() {

			@Override
			public String apply(BusRouteAggregationLight arg0) {
				return arg0.getRouteId();
			}
		}));
		
		for(String rn: routeNames) {
			Query<BusTrip> trip0q = ds.createQuery(BusTrip.class);
			trip0q.field("direction").equal("0");
			trip0q.field("routeId").equal(rn);
			BusTrip trip0 = trip0q.get();
			
			if(trip0 != null) {
				project = addCharger(project, trip0.getStops().get(0).getStopId(), charger, rn, minChargingTime);
				project = addCharger(project, trip0.getStops().get(trip0.getStops().size()-1).getStopId(), charger, rn, minChargingTime);
			}
			
			Query<BusTrip> trip1q = ds.createQuery(BusTrip.class);
			trip1q.field("direction").equal("1");
			trip1q.field("routeId").equal(rn);
			BusTrip trip1 = trip1q.get();
			
			if(trip1 != null) {
				project = addCharger(project, trip1.getStops().get(0).getStopId(), charger, rn, minChargingTime);
				project = addCharger(project, trip1.getStops().get(trip1.getStops().size()-1).getStopId(), charger, rn, minChargingTime);
			}
		}
		
		ds.save(project);
		return ok();
	}
	
	public static BusChargerInstance getChargerInstance(String chargerId) {
		BusCharger chargerType = ChargerController.getChargerModel(chargerId);
		BusChargerInstance chargerInstance = new BusChargerInstance();
		chargerInstance.setType(chargerType);
		return chargerInstance;
	}
	
	public static Result getAllRouteDates(String projectId) {
		ObjectMapper om = new ObjectMapper();
		PlanningProject project = getProjectObject(projectId);
		HashSet<String> routes = new HashSet<String>();
		for(BusRouteAggregationLight r: project.getRoutes()) {
			routes.add(r.getRouteId());
		}
		
		Set<String> dates = RoutesController.getRouteDates(routes);
		return ok(om.valueToTree(dates));
	}
	
	public static Result addRoute(String projectId) throws JsonProcessingException {
		PlanningProject project = getProjectObject(projectId);
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		
		JsonNode bodyJson = request().body().asJson();
		BusRouteAggregationLight route = om.treeToValue(bodyJson, BusRouteAggregationLight.class);
		Map<String, RouteDirection> dirs = RoutesController.getRouteWaypoints(route.getRouteId(), true);
		
		Set<Entry<String, RouteDirection>> entrs = dirs.entrySet();
		for(Entry<String, RouteDirection> e: entrs) {
			RouteDirection dir = e.getValue();
			if(dir == null) {
				continue;
			}
			
			for(int i=0; i<dir.getStops().size(); i++) {
				BusStop bs = dir.getStops().get(i);
				boolean endStop = i==0 || i==dir.getStops().size()-1;
				project.addStop(bs, endStop, route.getRouteId());
			}
		}
		project.getRoutes().add(route);
		ds.save(project);
		return ok();
	}

	public static Result updateProject(String projectId) {
		RequestBody body = request().body();
		ObjectMapper om = new ObjectMapper();
		
		try {
			PlanningProject proj = om.treeToValue(body.asJson(), PlanningProject.class);
			MongoUtils.ds().save(proj);
			return ok(om.valueToTree(proj));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return badRequest();
		}
	}
}
