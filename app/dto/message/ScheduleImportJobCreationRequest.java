package dto.message;

import play.mvc.Http.MultipartFormData;
import dto.jobstate.ScheduleImportJobState;

public class ScheduleImportJobCreationRequest {

	ScheduleImportJobState jobState;
	MultipartFormData body;
	
	public ScheduleImportJobCreationRequest(ScheduleImportJobState jobState,
			MultipartFormData body) {
		this.jobState = jobState;
		this.body = body;
	}

	public ScheduleImportJobState getJobState() {
		return jobState;
	}

	public void setJobState(ScheduleImportJobState jobState) {
		this.jobState = jobState;
	}

	public MultipartFormData getBody() {
		return body;
	}

	public void setBody(MultipartFormData body) {
		this.body = body;
	}
}
