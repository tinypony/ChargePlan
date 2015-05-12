package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.dataset.BusTrip;
import model.dataset.DayStat;
import model.dataset.aggregation.BusRouteAggregationLight;

import org.emn.calculate.bus.IConsumptionProfile;
import org.emn.calculate.bus.StaticConsumptionProfile;
import org.emn.calculate.price.DieselPricingModel;
import org.emn.calculate.price.EnergyPricingModel;
import org.emn.calculate.price.IEnergyPriceProvider;
import org.emn.calculate.route.DailyConsumptionModel;
import org.emn.calculate.route.Euro6EmissionModel;
import org.emn.calculate.route.IEmissionModel;
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
import utils.DateUtils;
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
		SimulationResult r = simulateBoth(proj, getReq(request()));
		proj = updateStat(proj, r);
		Datastore ds = MongoUtils.ds();
		ds.save(proj);
		return ok( om.valueToTree( r ) );
	}
	
	public static Result simulateAll(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();
		Datastore ds = MongoUtils.ds();
		SimulationRequest simreq = getReq(request());
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		List<SimulationResult> resultList = new ArrayList<SimulationResult>();
		
		for(BusRouteAggregationLight route: proj.getRoutes()) {
			simreq.setRouteId(route.getRouteId());
			SimulationResult r = simulateBoth(proj, simreq);
			proj = updateStat(proj, r);
			resultList.add(r);
		}
		ds.save(proj);
		return ok(om.valueToTree(resultList));
	}
	
	public static PlanningProject updateStat(PlanningProject proj, SimulationResult res) {
		
		if(res.getFeasibility() != null && res.getFeasibility().isSurvived()) {
			proj.getBusRoute(res.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
		} else if(res.getFeasibility() != null && !res.getFeasibility().isSurvived()) {
			proj.getBusRoute(res.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
		}
		
		Datastore ds = MongoUtils.ds();
		ds.save(res);
		
		return proj;
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
		res.setRouteId(simreq.getRouteId());
		res.setFeasibility(simulateRouteFeasibility(proj, simreq, getConsumptionProfile(simreq)));
		
		Set<String> dates = RoutesController.getRouteDates(new HashSet<String>(Arrays.asList(simreq.getRouteId())));
		List<CostSimulationResult> costs = new ArrayList<CostSimulationResult>();
		List<BusTrip> allTrips = RoutesController.getTrips(simreq.getRouteId(), null);
		
		for(String date: dates) {
			simreq.setDate(date);
			CostSimulationResult cres = simulateRouteCost(proj, simreq, allTrips);
			costs.add(cres);
		}
		res.setCost(costs);
		res.setType(simreq.getBusType());
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
	/*
	public static Result simulateRouteFeasibility(String projectId) throws Exception {
		ObjectMapper om = new ObjectMapper();	
		Datastore ds = MongoUtils.ds();
		JsonNode bodyJson = request().body().asJson();
		
		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
		PlanningProject proj = ProjectController.getProjectObject(projectId);
		
		
		FeasibilitySimulationResult result = simulateRouteFeasibility(proj, simreq, getConsumptionProfile(simreq));
		
		if(result.isSurvived()) {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
		} else {
			proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
		}
		
		ds.save(proj);
		return ok(om.valueToTree(result));
	}
	*/
//	public static Result simulateAllRoutesFeasibility(String projectId) throws Exception {
//		ObjectMapper om = new ObjectMapper();	
//		Datastore ds = MongoUtils.ds();
//		JsonNode bodyJson = request().body().asJson();
//		
//		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
//		PlanningProject proj = ProjectController.getProjectObject(projectId);
//		
//		
//		
//		List<FeasibilitySimulationResult> resultList = new ArrayList<FeasibilitySimulationResult>();
//		
//		for(BusRouteAggregationLight route: proj.getRoutes()) {
//			simreq.setRouteId(route.getRouteId());
//			FeasibilitySimulationResult result = simulateRouteFeasibility(proj, simreq, getConsumptionProfile(simreq));
//			resultList.add(result);
//			
//			if(result.isSurvived()) {
//				proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_OK);
//			} else {
//				proj.getBusRoute(simreq.getRouteId()).setState(BusRouteAggregationLight.State.SIMULATED_FAIL);
//			}
//		}
//		
//		ds.save(proj);
//		return ok(om.valueToTree(resultList));
//	}
	
	public static FeasibilitySimulationResult simulateRouteFeasibility(PlanningProject proj, SimulationRequest simreq, IConsumptionProfile profile) throws Exception {
		SimpleBusScheduler scheduler = new SimpleBusScheduler();
		List<BusTrip> trips = getTrips(simreq.getRouteId(), simreq.getDate());
		if(trips.size() == 0) return null;	
		scheduler.schedule(trips, proj.getStops());
		
		RouteSimulationModel simModel = new RouteSimulationModel( profile, new BusInstance(simreq.getBusType()), simreq.getDate());
		simModel.setElectrifiedStops(proj.getStops());
		simModel.setDistanceManager(new DistanceRetriever());
		simModel.setDirections(scheduler.getDirectionA(), scheduler.getDirectionB());
		FeasibilitySimulationResult result = simModel.simulate();
		return result;
	}
//	
//	public static Result simulateRouteCost(String projectId) throws Exception {
//		ObjectMapper om = new ObjectMapper();	
//		JsonNode bodyJson = request().body().asJson();
//		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
//		PlanningProject proj = ProjectController.getProjectObject(projectId);
//		return ok(om.valueToTree(simulateRouteCost(proj, simreq)));
//	}
//	
//	public static Result simulateAllRoutesCost(String projectId) throws Exception {
//		ObjectMapper om = new ObjectMapper();	
//		JsonNode bodyJson = request().body().asJson();
//		SimulationRequest simreq = om.treeToValue(bodyJson, SimulationRequest.class);
//		PlanningProject proj = ProjectController.getProjectObject(projectId);
//		List<CostSimulationResult> results = new ArrayList<CostSimulationResult>();
//		
//		for(BusRouteAggregationLight route: proj.getRoutes()) {
//			simreq.setRouteId(route.getRouteId());
//			results.add(simulateRouteCost(proj, simreq));
//		}
//		
//		return ok(om.valueToTree(results));
//	}
	
	//cost
	public static CostSimulationResult simulateRouteCost(PlanningProject proj, final SimulationRequest simreq, List<BusTrip> trips) throws Exception {
		CostSimulationResult result = new CostSimulationResult();
		Calendar cal = DateUtils.getCalendar(simreq.getDate());
		IEmissionModel emissionsModel = new Euro6EmissionModel();
		Double energyPrice = 0.0;
		
		result.setRouteId(simreq.getRouteId());
		result.setDate(simreq.getDate());
		
		//Get all electrified bus stops on the route
		List<ElectrifiedBusStop> elStops = Lists.newArrayList(Iterables.filter(proj.getStops(), new Predicate<ElectrifiedBusStop>() {
			public boolean apply(ElectrifiedBusStop stop) {
				return stop.getCharger()!=null && stop.getChargingTimes().keySet().contains(simreq.getRouteId());
			}
		}));
		
		
		
		EnergyPricingModel enModel = new EnergyPricingModel(new IEnergyPriceProvider() {
			@Override
			public Double getMWhPrice(Date time) {
				return 220.0;
			}
		});

		List<BusTrip> dateTrips = Lists.newArrayList(Iterables.filter(trips, new Predicate<BusTrip>() {
			@Override
			public boolean apply(BusTrip arg0) {
				return arg0.getDates().contains(simreq.getDate());
			}
		}));
		
		for(ElectrifiedBusStop stop: elStops) {
			DailyConsumptionModel consumptionModel = StopsController.getStopConsumptionModel(stop, cal, dateTrips);
			Double tmp = enModel.getEnergyCost(Arrays.asList(consumptionModel), simreq.getRouteId());
			energyPrice += tmp;
		}
		
		result.setMetersDriven(getTotalDistanceDriven(dateTrips));
		result.setEnergyPrice(energyPrice);
		result.setDieselPrice(DieselPricingModel.getCost(result.getMetersDriven()));
		
		DayStat st = new DayStat();
		st.setTotalDistance(result.getMetersDriven());
		result.setEmissions(emissionsModel.getDailyEmissions(null, st));
		return result;
	}
	
	private static int getTotalDistanceDriven(List<BusTrip> trips) {
		int metersDriven = 0;
		
		for(BusTrip trip: trips) {
			metersDriven += trip.getTripLength();
		}
		return metersDriven;
	}
	
	
	private static List<BusTrip> getTrips(String routeId, String date) {
		String formattedDate = (new SimpleDateFormat("YYYY-M-d")).format(DateUtils.getCalendar(date).getTime());
		Datastore ds = MongoUtils.ds();
		Query<BusTrip> tripsQ = ds.createQuery(BusTrip.class);
		tripsQ.field("routeId").equal(routeId);
		tripsQ.field("dates").equal(formattedDate);
		List<BusTrip> trips = tripsQ.asList();
		
		return trips;
	}
}
