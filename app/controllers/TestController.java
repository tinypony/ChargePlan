package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.RouteGroup;
import model.sql.BusStop;
import model.sql.BusTrip;

import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBRef;

import static org.mongodb.morphia.aggregation.Group.*;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;


public class TestController extends Controller {

	public static Result getTest() {
		AggregationPipeline<BusTrip, RouteGroup> pipeline = MongoUtils.ds().createAggregation(BusTrip.class);
		ArrayList<Group> idGroup = new ArrayList<Group>();
		idGroup.add(grouping("serviceNbr"));
		idGroup.add(grouping("direction"));
		
		pipeline.group(
				idGroup, 
				grouping("serviceNbr", first("serviceNbr")), 
				grouping("direction", first("direction")),
				grouping("referencedStops", first("stops"))
		);
		
		MorphiaIterator<RouteGroup, RouteGroup> groups = pipeline.aggregate(RouteGroup.class);
		Iterator<RouteGroup> it = groups.iterator();
		ArrayList<RouteGroup> distRoutes = new ArrayList<RouteGroup>();
		Mapper mapper = new Mapper();
		EntityCache cache = mapper.createEntityCache();
		
		while(it.hasNext()) {
			RouteGroup rg = it.next();
			
			for(DBRef stopRef : rg.referencedStops) {
				Logger.info(""+stopRef.getClass());
				//rg.stops.add( mapper.fromDBObject(BusStop.class, stopRef.fetch(), cache));
			}
			
			distRoutes.add(rg);
		}
		
		ObjectMapper om = new ObjectMapper();
		
		return ok(om.valueToTree(distRoutes));
	}
}
