package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.dataset.ScheduleStop;
import model.dataset.BusStop;
import model.dataset.BusTrip;
import model.dataset.aggregation.BusRouteAggregationLight;

import org.emn.calculate.route.ChargerEnergyConsumptionModel;
import org.emn.calculate.route.DailyConsumptionModel;
import org.emn.calculate.route.HourlyConsumptionEntry;
import org.emn.plan.model.ElectrifiedBusStop;
import org.emn.plan.model.PlanningProject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import play.mvc.Controller;
import play.mvc.Result;
import utils.DateUtils;
import utils.MongoUtils;

public class StopsController  extends Controller {
	
	public static Result getStops() {
		Datastore ds = MongoUtils.ds();
		Query<BusStop> stops = ds.createQuery(BusStop.class);
		
		Map<String, BusStop> map = new HashMap<String, BusStop>();
		for(BusStop bs : stops.asList()) {
			map.put(bs.getStopId(), bs);
		}
		
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(map)).as("application/json");
	}
	
	public static BusStop getStopModel(String stopid) {
		Datastore ds = MongoUtils.ds();
		Query<BusStop> busStopQ = ds.createQuery(BusStop.class);
		busStopQ.field("stopId").equal(stopid);
		return busStopQ.get();		
	}
	
	public static Result getStop(String stopid) {
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(getStopModel(stopid)));
	}

	public static Result getElectrifiedStopConsumption(String projectId, String stopId) throws ParseException {
		ObjectMapper om = new ObjectMapper();
		
		PlanningProject project = ProjectController.getProjectObject(projectId);
		ElectrifiedBusStop elStop = project.getElectrifiedStop(stopId);
		Set<String> elBusRoutes = getBusRoutesThroughStop(project, elStop);
		Set<String> dates = RoutesController.getRouteDates(elBusRoutes);
		Calendar cal = DateUtils.getCalendar(dates.iterator().next());
		
		Map<Integer, List<HourlyConsumptionEntry>> consumptionMap 
			= getStopConsumptionModel(elStop, cal, elBusRoutes).getHourlyConsumptionDistribution();

		return ok(om.valueToTree(consumptionMap));
	}
	
	public static DailyConsumptionModel getStopConsumptionModel(ElectrifiedBusStop elStop, Calendar cal, Set<String> elBusRoutes) {
		if(elStop == null || elBusRoutes.size() == 0) {
			return new DailyConsumptionModel(cal);
		}
		
		Datastore ds = MongoUtils.ds();
		Query<BusTrip> q = ds.createQuery(BusTrip.class);
		System.out.println(DateUtils.toString(cal, "YYYY-M-d"));
		q.field("dates").equal(DateUtils.toString(cal, "YYYY-M-d"));
		System.out.println(elBusRoutes);
		q.field("routeId").in(Lists.newArrayList(elBusRoutes));
		
		
		List<BusTrip> trips = q.asList();
		ChargerEnergyConsumptionModel consumptionModel = 
				new ChargerEnergyConsumptionModel(elStop, cal, trips);
		
		return consumptionModel.getEnergyConsumption();
	}
	
	public static Set<String> getBusRoutesThroughStop(PlanningProject project, ElectrifiedBusStop elStop) {
		
		Set<String> busRoutes = Sets.newHashSet(Iterables.transform(project.getRoutes(), 
				new Function<BusRouteAggregationLight, String>() {

			@Override
			public String apply(BusRouteAggregationLight arg0) {
				return arg0.getRouteId();
			}
		}));
		
		
		if(elStop==null || elStop.getChargingTimes().keySet().size() == 0) {
			return new HashSet<String>();
		} else {
			return Sets.intersection(busRoutes, elStop.getChargingTimes().keySet());
		}
	}
}
