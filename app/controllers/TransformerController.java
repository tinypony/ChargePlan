package controllers;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.dataset.Transformer;
import play.mvc.Controller;
import play.mvc.Result;
import utils.MongoUtils;

public class TransformerController extends Controller {

	public static Result getTransformers() {
		Datastore ds = MongoUtils.ds();
		Query<Transformer> q = ds.createQuery(Transformer.class);
		ObjectMapper om = new ObjectMapper();
		
		return ok(om.valueToTree(q.asList()));
	}
}
