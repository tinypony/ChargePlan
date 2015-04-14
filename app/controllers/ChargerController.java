package controllers;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import model.planning.BusCharger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.RequestBody;
import utils.MongoUtils;

public class ChargerController extends Controller {

	//POST
	public static Result createCharger() {
		RequestBody body = request().body();
		ObjectMapper om = new ObjectMapper();
		try {
			BusCharger charger = om.treeToValue(body.asJson(), BusCharger.class);
			Datastore ds = MongoUtils.ds();
			ds.save(charger);
			return created(om.valueToTree(charger));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return badRequest();
		}
	}
	
	//PUT
	public static Result updateCharger(String hexString) {
		ObjectId id = new ObjectId(hexString);
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		BusCharger charger = ds.createQuery(BusCharger.class).field("id").equal(id).get();
		
		try {
			BusCharger updated = om.treeToValue(request().body().asJson(), BusCharger.class);
			charger.setManufacturer(updated.getManufacturer());
			charger.setModel(updated.getModel());
			charger.setPower(updated.getPower());
			ds.save(charger);
			return ok(om.valueToTree(charger));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return badRequest("Could not parse body as BusCharger");
		}
	}
	
	//GET
	public static Result listChargers() {
		Datastore ds = MongoUtils.ds();
		ObjectMapper om = new ObjectMapper();
		return ok(om.valueToTree(ds.createQuery(BusCharger.class).asList()));
	}
}
