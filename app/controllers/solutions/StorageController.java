package controllers.solutions;

import model.planning.solutions.BusCharger;
import model.planning.solutions.ElectricBus;
import model.planning.solutions.EnergyStorage;

import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.RequestBody;
import utils.MongoUtils;

public class StorageController extends Controller {

	public static Result createStorage() {
		RequestBody body = request().body();
		ObjectMapper om = new ObjectMapper();
		try {
			EnergyStorage newStorage = om.treeToValue(body.asJson(), EnergyStorage.class);
			Datastore ds = MongoUtils.ds();
			ds.save(newStorage);
			return created(om.valueToTree(newStorage));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return badRequest();
		}
	}
	
	public static Result listStorageType() {
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(ds.createQuery(EnergyStorage.class).asList()));
	}
}
