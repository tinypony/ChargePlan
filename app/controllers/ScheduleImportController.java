package controllers;

import java.io.IOException;

import dto.jobstate.ScheduleImportJobState;
import dto.message.ScheduleImportJobCreationRequest;
import actors.JobManagerActor;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class ScheduleImportController extends Controller {

	public static Result uploadGtfs() throws IOException, InterruptedException {
		
		final ScheduleImportJobState jobState = JobManagerActor.createScheduleImportJobState();
		final MultipartFormData body = request().body().asMultipartFormData();

		F.Promise.promise(new F.Function0<Object>() {
			@Override
			public Object apply() throws Throwable {
				ScheduleImportJobCreationRequest msg = new ScheduleImportJobCreationRequest(jobState, body);
				JobManagerActor.actor().tell(msg, null);
                return null;
			}
		});
		
		return ok("Job created");
	}
}
