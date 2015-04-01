package controllers;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import model.BusRoute;
import model.BusStop;
import model.BusTrip;
import model.ScheduleStop;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.base.Function;
import com.mongodb.DBCollection;

import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class RoutesController extends Controller {

	public static Result getRoutes() {
		Datastore ds = MongoUtils.ds();
		DBCollection routesColl = ds.getCollection(BusRoute.class);
		
		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		q.field("name").equal(Pattern.compile("^N{0,1}\\d\\d[A-Za-z]{0,1}$"));
		List<BusRoute> routes =  q.asList();
//		
//		for(BusRoute r: routes) {
//			Query<BusTrip> qr = ds.createQuery(BusTrip.class);
//			BusTrip trip = qr.field("routeId").equal(r.getRouteId()).get();
//			List<ScheduleStop> stops = trip.getStops();
//			Collections.sort(stops);
//			
//			List<BusStop> waypoints = Lists.transform(stops, new Function<ScheduleStop, BusStop>(){
//				public BusStop apply(ScheduleStop s){
//					return s.getStop();
//				}
//			});
//			
//			r.setWaypoints(waypoints);
//		}
		
		
		ObjectMapper om = new ObjectMapper();
		
		return ok(om.valueToTree(routes)).as("application/json");
	}
}
