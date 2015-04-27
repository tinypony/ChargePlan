package controllers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import model.dataset.BusTrip;
import model.dataset.aggregation.BusRouteAggregationLight;
import model.planning.BusInstance;
import model.planning.ElectrifiedBusStop;
import model.planning.PlanningProject;

import org.emn.calculate.EnergyPricingModel;
import org.emn.calculate.bus.StaticConsumptionProfile;
import org.emn.calculate.route.RouteSimulationModel;
import org.emn.plan.SimpleBusScheduler;
import org.emn.plan.SimulationResult;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import configuration.emn.route.DistanceRetriever;
import dto.message.client.SimulationRequest;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DateUtils;
import utils.MongoUtils;

public class SimulationController extends Controller {

	//Feasibility
	public static Result simulateRouteFeasibility(String projectId) throws Exception {
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
	
	public static Result simulateRouteCost(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		JsonNode bodyJson = request().body().asJson();
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		return ok(""+simulateRouteCost(projectId, simreq));
	}
	
	public static Double simulateRouteCost(String projectId, final SimulationRequest simreq) throws Exception {
		Datastore ds = MongoUtils.ds();
		
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		//Get all electrified bus stops on the route
		List<ElectrifiedBusStop> elStops =  Lists.newArrayList(Iterables.filter(proj.getStops(), new Predicate<ElectrifiedBusStop>() {
			public boolean apply(ElectrifiedBusStop stop) {
				return stop.getChargingTimes().keySet().contains(simreq.getRouteId());
			}
		}));
		Calendar cal = DatatypeConverter.parseDate(simreq.getDate());
		EnergyPricingModel enModel = new EnergyPricingModel();
		Double result = 0.0;
		
		for(ElectrifiedBusStop stop: elStops) {
			Set<String> routes = new HashSet<String>();
			routes.add(simreq.getRouteId());
			Map<Long, Double> consumptionMap = StopsController.getStopConsumptionMap(stop, cal, routes);
			result += enModel.getEnergyCost(consumptionMap);
		}
		
		return result;
	}
	
	private static List<BusTrip> getTrips(String routeId, String date) {
		Datastore ds = MongoUtils.ds();
		Query<BusTrip> tripsQ = ds.createQuery(BusTrip.class);
		tripsQ.field("routeId").equal(routeId);
		tripsQ.field("dates").equals(date);
		return tripsQ.asList();
	}
}
