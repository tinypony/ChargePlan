package controllers;

import java.util.List;

import model.dataset.BusStop;
import model.planning.BusChargerInstance;
import model.planning.ElectrifiedBusStop;
import model.planning.PlanningProject;
import model.planning.solutions.BusCharger;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	public static Result addCharger(String projectId) {
		Datastore ds = MongoUtils.ds();
		JsonNode bodyJson = request().body().asJson();
		String chargerId = bodyJson.get("chargerType").asText();
		String stopId = bodyJson.get("stop").asText();
		ObjectId id = new ObjectId(projectId);
		
		Query<PlanningProject> q = ds.createQuery(PlanningProject.class);
		q.field("id").equals(id);
		PlanningProject project = q.get();
		
		project = addCharger(project, stopId, chargerId);
		
		ds.save(project);
		return ok();
	}
	
	public static PlanningProject addCharger(PlanningProject project, String stopId, String chargerId) {
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
	
	public static BusChargerInstance getChargerInstance(String chargerId) {
		BusCharger chargerType = ChargerController.getChargerModel(chargerId);
		BusChargerInstance chargerInstance = new BusChargerInstance();
		chargerInstance.setType(chargerType);
		return chargerInstance;
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
