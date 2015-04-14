package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import model.dataset.BusTrip;
import model.calculation.Euro6EmissionModel;
import model.calculation.IEmissionModel;
import model.dataset.BusRoute;
import model.dataset.DayStat;
import model.dataset.DetailedBusRoute;
import model.dataset.RouteDirection;
import model.dataset.aggregation.BusRouteAggregation;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;

import static org.mongodb.morphia.aggregation.Group.*;

import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class RoutesController extends Controller {
	
	

	public static Result getRoutes() {
		Datastore ds = MongoUtils.ds();
		Map<String, String[]> qs = request().queryString();
		ObjectMapper om = new ObjectMapper();
		IEmissionModel eModel = new Euro6EmissionModel();
		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		q.field("name").equal(Pattern.compile("^N{0,1}\\d\\d[A-Za-z]{0,1}$"));
		List<BusRoute> routes = q.asList();
		
	//	if(qs.get("aggregate") != null && "true".equalsIgnoreCase(qs.get("aggregate")[0])) {
			List<BusRouteAggregation> aggr = new ArrayList<BusRouteAggregation>();
					
			for(BusRoute r: routes) {
				BusRouteAggregation aggregate = new BusRouteAggregation();
				aggregate.setName(r.getName());
				aggregate.setLongName(r.getLongName());
				aggregate.setRouteId(r.getRouteId());
				
				for(DayStat st: r.getStats()) {
					aggregate.addStat(st);
				}
				aggr.add(aggregate);
			}
			
			for(BusRouteAggregation ag: aggr) {
				ag.setEmissions(eModel.getDailyEmissions(ag));
			}
			
			return ok(om.valueToTree(aggr));
//		} else {
//			for (BusRoute r : routes) {
//				for (DayStat stat : r.getStats()) {
//					stat.setEmissions(eModel.getDailyEmissions(r, stat));
//				}
//			}
//
//			return ok(om.valueToTree(routes)).as("application/json");
//		}
	}
	
	public static Result getRouteWaypoints(String routeId) {
		Datastore ds = MongoUtils.ds();
		
		Query<BusTrip> trip0Q = ds.createQuery(BusTrip.class);
		trip0Q.field("routeId").equal(routeId);
		trip0Q.field("direction").equal("0");
		trip0Q.order("-numOfStops");

		Query<BusTrip> trip1Q = ds.createQuery(BusTrip.class);
		trip1Q.field("routeId").equal(routeId);
		trip1Q.field("direction").equal("1");
		trip1Q.order("-numOfStops");
		
		RouteDirection dir0 = new RouteDirection();
		dir0.setStops(trip0Q.get().getStops());
		
		RouteDirection dir1 = new RouteDirection();
		dir1.setStops(trip1Q.get().getStops());
		
		HashMap<String, RouteDirection> directions = new HashMap<String, RouteDirection>();
		directions.put("0", dir0);
		
		directions.put("1", dir1);
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(directions));
	}

	public static Result getRouteDetails(String routeName) {
		Datastore ds = MongoUtils.ds();
		List<DetailedBusRoute> instances = new ArrayList<DetailedBusRoute>();

		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		q.field("name").equal(routeName);
		List<BusRoute> routeInstances = q.asList();

		for (BusRoute inst : routeInstances) {
			DetailedBusRoute det = new DetailedBusRoute(inst);
			instances.add(det);
		}

		Query<BusTrip> trip0Q = ds.createQuery(BusTrip.class);
		trip0Q.field("routeId").equal(instances.get(0).getRouteId());
		trip0Q.field("direction").equal("0");
		trip0Q.order("-numOfStops");

		Query<BusTrip> trip1Q = ds.createQuery(BusTrip.class);
		trip1Q.field("routeId").equal(instances.get(0).getRouteId());
		trip1Q.field("direction").equal("1");
		trip1Q.order("-numOfStops");

		List<BusTrip> trips = Lists.newArrayList(trip0Q.get(), trip1Q.get());

		instances.get(0).setTrips(trips);

		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(instances));
	}
}
