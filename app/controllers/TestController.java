package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.dataset.BusTrip;
import model.dataset.BusTripGroup;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.query.MorphiaIterator;

import static org.mongodb.morphia.aggregation.Group.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class TestController extends Controller {

	public static Result getTest() {
		Datastore ds = MongoUtils.ds();
		AggregationPipeline<BusTrip, BusTripGroup> ap = ds.createAggregation(BusTrip.class);
		
		List<Group> idGroup = id(grouping("routeId"), grouping("direction"));
		ap.group(idGroup, grouping("routeId", first("routeId")), grouping("direction", first("direction")), grouping("stops", first("stops")));
		
		MorphiaIterator<BusTripGroup, BusTripGroup> iterator = ap.aggregate(BusTripGroup.class);
		Iterator<BusTripGroup> it = iterator.iterator();
		List<BusTripGroup> list = new ArrayList<BusTripGroup>();
		while(it.hasNext()) {
			list.add(it.next());
		}
		ObjectMapper om = new ObjectMapper();
		
		return ok(om.valueToTree(list)).as("application/json");
	}
}
