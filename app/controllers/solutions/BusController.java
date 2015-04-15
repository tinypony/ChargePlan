package controllers.solutions;

import model.planning.solutions.BusCharger;
import model.planning.solutions.ElectricBus;

import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.RequestBody;
import utils.MongoUtils;

public class BusController extends Controller {

	public static Result createBus() {
		RequestBody body = request().body();
		ObjectMapper om = new ObjectMapper();
		try {
			ElectricBus newBus = om.treeToValue(body.asJson(), ElectricBus.class);
			Datastore ds = MongoUtils.ds();
			ds.save(newBus);
			return created(om.valueToTree(newBus));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return badRequest();
		}
	}
	
	public static Result listBuses() {
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(ds.createQuery(ElectricBus.class).asList()));
	}
}
