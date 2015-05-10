package controllers;

import java.text.ParseException;
import java.util.ArrayList;
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

import org.emn.calculate.bus.IConsumptionProfile;
import org.emn.calculate.bus.StaticConsumptionProfile;
import org.emn.calculate.price.DieselPricingModel;
import org.emn.calculate.price.EnergyPricingModel;
import org.emn.calculate.price.IEnergyPriceProvider;
import org.emn.calculate.route.DailyConsumptionModel;
import org.emn.calculate.route.RouteSimulationModel;
import org.emn.plan.SimpleBusScheduler;
import org.emn.plan.model.BusInstance;
import org.emn.plan.model.ElectrifiedBusStop;
import org.emn.plan.model.PlanningProject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import configuration.emn.route.DistanceRetriever;
import dto.message.client.CostSimulationResult;
import dto.message.client.FeasibilitySimulationResult;
import dto.message.client.SimulationRequest;
import dto.message.client.SimulationResult;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import utils.MongoUtils;

public class SimulationController extends Controller {

	/**
	 * Endpoint for retrieving result that contains both feasibility and cost simulation results
	 * @param projectId
	 * @return
	 * @throws IllegalArgumentException
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public static Result simulateBoth(String projectId) throws IllegalArgumentException, JsonProcessingException, Exception {
		ObjectMapper om = new ObjectMapper();
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		return ok( om.valueToTree( simulateBoth(proj, getReq(request())) ) );
		
	}
	
	/**
	 * Logic that runs both simulations for a route
	 * @param proj
	 * @param simreq
	 * @return
	 * @throws Exception
	 */
	public static SimulationResult simulateBoth(PlanningProject proj, SimulationRequest simreq) throws Exception {
		SimulationResult res = new SimulationResult();
		res.setFeasibility(simulateRouteFeasibility(proj, simreq, getConsumptionProfile(simreq)));
		res.setCost(simulateRouteCost(proj, simreq));
		return res;
	}
	
	public static IConsumptionProfile getConsumptionProfile(SimulationRequest simreq) {
		StaticConsumptionProfile profile = new StaticConsumptionProfile();
		profile.setConsumption(2.5);
		return profile;
	}
	
	private static SimulationRequest getReq(Request req) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		JsonNode bodyJson = req.body().asJson();
		return om.treeToValue(bodyJson, SimulationRequest.class);
	}
	
	//Feasibility
	public static Result simulateRouteFeasibility(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		Datastore ds = MongoUtils.ds();
		JsonNode bodyJson = request().body().asJson();
		
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		StaticConsumptionProfile profile = new StaticConsumptionProfile();
		profile.setConsumption(2.5);
		
		FeasibilitySimulationResult result = simulateRouteFeasibility(proj, simreq, profile);
		
		if(result.isSurvived()) {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
		} else {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
		}
		
		ds.save(proj);
		return ok(om.valueToTree(result));
	}
	
	public static Result simulateAllRoutesFeasibility(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		Datastore ds = MongoUtils.ds();
		JsonNode bodyJson = request().body().asJson();
		
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		
		
		List<FeasibilitySimulationResult> resultList = new ArrayList<FeasibilitySimulationResult>();
		
		for(BusRouteAggregationLight route: proj.getRoutes()) {
			simreq.setRouteId(route.getRouteId());
			FeasibilitySimulationResult result = simulateRouteFeasibility(proj, simreq, getConsumptionProfile(simreq));
			resultList.add(result);
			
			if(result.isSurvived()) {
				proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
			} else {
				proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
			}
		}
		
		ds.save(proj);
		return ok(om.valueToTree(resultList));
	}
	
	public static FeasibilitySimulationResult simulateRouteFeasibility(PlanningProject proj, SimulationRequest simreq, IConsumptionProfile profile) throws Exception {
		SimpleBusScheduler scheduler = new SimpleBusScheduler();
		scheduler.schedule(getTrips(simreq.getRouteId(), simreq.getDate()), proj.getStops());
		
		RouteSimulationModel simModel = new RouteSimulationModel( profile, new BusInstance(simreq.getBusType()), simreq.getDate());
		simModel.setElectrifiedStops(proj.getStops());
		simModel.setDistanceManager(new DistanceRetriever());
		simModel.setDirections(scheduler.getDirectionA(), scheduler.getDirectionB());
		FeasibilitySimulationResult result = simModel.simulate();
		return result;
	}
	
	public static Result simulateRouteCost(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		JsonNode bodyJson = request().body().asJson();
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		return ok(om.valueToTree(simulateRouteCost(proj, simreq)));
	}
	
	public static Result simulateAllRoutesCost(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		JsonNode bodyJson = request().body().asJson();
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		List<CostSimulationResult> results = new ArrayList<CostSimulationResult>();
		
		for(BusRouteAggregationLight route: proj.getRoutes()) {
			simreq.setRouteId(route.getRouteId());
			results.add(simulateRouteCost(proj, simreq));
		}
		
		return ok(om.valueToTree(results));
	}
	
	//cost
	public static CostSimulationResult simulateRouteCost(PlanningProject proj, final SimulationRequest simreq) throws Exception {
		CostSimulationResult result = new CostSimulationResult();
		
		result.setRouteId(simreq.getRouteId());
		//Get all electrified bus stops on the route
		List<ElectrifiedBusStop> elStops =  Lists.newArrayList(Iterables.filter(proj.getStops(), new Predicate<ElectrifiedBusStop>() {
			public boolean apply(ElectrifiedBusStop stop) {
				return stop.getCharger()!=null && stop.getChargingTimes().keySet().contains(simreq.getRouteId());
			}
		}));
		
		Calendar cal = DatatypeConverter.parseDate(simreq.getDate());
		EnergyPricingModel enModel = new EnergyPricingModel(new IEnergyPriceProvider() {
			@Override
			public Double getMWhPrice(Date time) {
				return 220.0;
			}
		});
		
		Double energyPrice = 0.0;
		
		for(ElectrifiedBusStop stop: elStops) {
			DailyConsumptionModel consumptionModel = StopsController
					.getStopConsumptionModel(stop, cal, StopsController.getBusRoutesThroughStop(proj, stop));
			
			energyPrice += enModel.getEnergyCost(Arrays.asList(consumptionModel), simreq.getRouteId());
		}
		
		result.setMetersDriven(getTotalDistanceDriven(simreq));
		result.setEnergyPrice(energyPrice);
		result.setDieselPrice(DieselPricingModel.getCost(result.getMetersDriven()));
		return result;
	}
	
	private static int getTotalDistanceDriven(SimulationRequest simreq) {
		List<BusTrip> trips = getTrips(simreq.getRouteId(), simreq.getDate());
		int metersDriven = 0;
		for(BusTrip trip: trips) {
			metersDriven += trip.getTripLength();
		}
		return metersDriven;
	}
	
	private static List<BusTrip> getTrips(String routeId, String date) {
		Datastore ds = MongoUtils.ds();
		Query<BusTrip> tripsQ = ds.createQuery(BusTrip.class);
		tripsQ.field("routeId").equal(routeId);
		tripsQ.field("dates").equals(date);
		List<BusTrip> trips = tripsQ.asList();
		return trips;
	}
}
