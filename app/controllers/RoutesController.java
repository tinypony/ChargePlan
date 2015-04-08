package controllers;

import java.util.List;
import java.util.regex.Pattern;

import model.calculation.Euro6EmissionModel;
import model.calculation.IEmissionModel;
import model.dataset.BusRoute;
import model.dataset.DayStat;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
