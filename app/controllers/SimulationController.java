package controllers;

import java.util.List;

import model.dataset.BusTrip;
import model.dataset.aggregation.BusRouteAggregationLight;
import model.planning.BusInstance;
import model.planning.PlanningProject;

import org.emn.calculate.StaticConsumptionProfile;
import org.emn.plan.RouteSimulationModel;
import org.emn.plan.SimpleBusScheduler;
import org.emn.plan.SimulationResult;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import configuration.emn.route.DistanceRetriever;
import dto.message.client.SimulationRequest;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class SimulationController extends Controller {

	public static Result simulateRoute(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		JsonNode bodyJson = request().body().asJson();
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		
		Datastore ds = MongoUtils.ds();
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		SimpleBusScheduler scheduler = new SimpleBusScheduler();
		scheduler.schedule(getTrips(simreq.getRouteId(), simreq.getDate()), proj.getStops());
		
		StaticConsumptionProfile profile = new StaticConsumptionProfile();
		profile.setConsumption(2.5);
		
		RouteSimulationModel simModel = new RouteSimulationModel( profile, new BusInstance(simreq.getBusType()), simreq.getDate());
		simModel.setElectrifiedStops(proj.getStops());
		simModel.setDistanceManager(new DistanceRetriever());
		simModel.setDirections(scheduler.getDirectionA(), scheduler.getDirectionB());
		SimulationResult result = simModel.simulate();
		
		if(result.isSurvived()) {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
		} else {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
		}
		
		ds.save(proj);
		
		return ok(om.valueToTree(result));
	}
	
	private static List<BusTrip> getTrips(String routeId, String date) {
		Datastore ds = MongoUtils.ds();
		Query<BusTrip> tripsQ = ds.createQuery(BusTrip.class);
		tripsQ.field("routeId").equal(routeId);
		tripsQ.field("dates").equals(date);
		return tripsQ.asList();
	}
}
