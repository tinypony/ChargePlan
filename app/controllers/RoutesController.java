package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import model.dataset.BusTrip;
import model.calculation.Euro6EmissionModel;
import model.calculation.IEmissionModel;
import model.dataset.BusRoute;
import model.dataset.DayStat;
import model.planning.DetailedBusRoute;

import org.mongodb.morphia.Datastore;
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
		
		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		q.field("name").equal(Pattern.compile("^N{0,1}\\d\\d[A-Za-z]{0,1}$"));
		List<BusRoute> routes =  q.asList();
		
		IEmissionModel eModel = new Euro6EmissionModel();
		for(BusRoute r: routes) {
			for(DayStat stat: r.getStats()) {
				stat.setEmissions(eModel.getDailyEmissions(r, stat));
			}
		}		
		
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(routes)).as("application/json");
	}
	
	public static Result getRouteDetails(String routeName) {
		Datastore ds = MongoUtils.ds();
		List<DetailedBusRoute> instances = new ArrayList<DetailedBusRoute>();
		
		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		q.field("name").equal(routeName);
		List<BusRoute> routeInstances = q.asList();
		
		for(BusRoute inst: routeInstances) {
			DetailedBusRoute det = new DetailedBusRoute(inst);
			instances.add(det);
		}
		
		Query<BusTrip> trip0Q = ds.createQuery(BusTrip.class);
		trip0Q.field("routeId").equal(instances.get(0).getRouteId());
		trip0Q.field("direction").equal("0");
		
		Query<BusTrip> trip1Q = ds.createQuery(BusTrip.class);
		trip1Q.field("routeId").equal(instances.get(0).getRouteId());
		trip1Q.field("direction").equal("1");
	
		List<BusTrip> trips = Lists.newArrayList(trip0Q.get(), trip1Q.get());
		
		instances.get(0).setTrips(trips);
		
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(instances));
	}
}
