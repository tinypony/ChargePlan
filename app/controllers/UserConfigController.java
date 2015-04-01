package controllers;

import model.ClientConfig;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import utils.MongoUtils;
import play.mvc.Result;

public class UserConfigController extends Controller {
	
	
	public static Result getConfig() {
		Datastore ds = MongoUtils.ds();
        Query<ClientConfig> query = ds.find(ClientConfig.class);
        ClientConfig config = query.get();
        
        ObjectMapper om = new ObjectMapper();
        JsonNode jn = om.valueToTree(config);
        return ok(jn);
	}
}
