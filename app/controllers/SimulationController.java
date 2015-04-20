package controllers;

import java.util.List;

import model.dataset.BusTrip;
import model.planning.PlanningProject;

import org.emn.plan.SimpleBusScheduler;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dto.message.client.SimulationRequest;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class SimulationController extends Controller {

	public static Result simulateRoute(String projectId) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();	
		JsonNode bodyJson = request().body().asJson();
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		
		Datastore ds = MongoUtils.ds();
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		Query<BusTrip> tripsQ = ds.createQuery(BusTrip.class);
		tripsQ.field("routeId").equal(simreq.getRouteId());
		tripsQ.field("dates").equals(simreq.getDate());
		List<BusTrip> trips = tripsQ.asList();
		
		SimpleBusScheduler scheduler = new SimpleBusScheduler();
		
		return ok();
	}
}
