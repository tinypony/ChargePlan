package controllers;

import java.util.ArrayList;
import java.util.List;

import models.BusTrip;
import models.BusTripGroup;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import static org.mongodb.morphia.aggregation.Group.*;

import com.mongodb.Mongo;

import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class TestController extends Controller {

	public static Result getTest() {
		Datastore ds = MongoUtils.ds();
		AggregationPipeline<BusTrip, BusTripGroup> ap = ds.createAggregation(BusTrip.class);
		
		List<Group> idGroup = id(grouping("serviceNbr"), grouping("direction"));
		
		ap.group(idGroup, grouping("stops", first("stops")));
		return ok();
	}
}
