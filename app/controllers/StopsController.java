package controllers;

import java.util.HashMap;
import java.util.Map;

import model.BusStop;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;
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

}