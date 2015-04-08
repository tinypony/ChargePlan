package controllers;

import java.util.List;

import model.planning.PlanningProject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.MongoUtils;

public class ProjectController extends Controller {
	
	public static Result getProjects() {
		Datastore ds = MongoUtils.ds();
		List<PlanningProject> projects = ds.createQuery(PlanningProject.class).asList();
		return ok((new ObjectMapper()).valueToTree(projects)).as("application/json");
	}
	
	public static Result getProject(String projectId) {
		Datastore ds = MongoUtils.ds();
		ObjectId id = new ObjectId(projectId);
		
		Query<PlanningProject> q = ds.createQuery(PlanningProject.class);
		q.field("id").equals(id);
		
		return ok((new ObjectMapper()).valueToTree(q.get())).as("application/json");
	}
	
	public static Result createProject() {
		Datastore ds = MongoUtils.ds();
		PlanningProject proj = new PlanningProject();
		ds.save(proj);
		
		return created((new ObjectMapper()).valueToTree(proj)).as("application/json");
	}

	public static Result updateProject(String projectId) {
		RequestBody body = request().body();
		ObjectMapper om = new ObjectMapper();
		
		try {
			PlanningProject proj = om.treeToValue(body.asJson(), PlanningProject.class);
			MongoUtils.ds().save(proj);
			return ok(om.valueToTree(proj));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return badRequest();
		}
	}
}
