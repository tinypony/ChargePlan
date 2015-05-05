package dto.jobstate;

import java.util.Arrays;

import org.mongodb.morphia.annotations.Entity;

import actors.JobMonitoringActor;
import akka.actor.ActorRef;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import dto.message.JobProgressMessage;

@Entity("jobs")
public class ScheduleImportJobState extends JobState {

	public enum JobState {
		UPLOAD(0, "Upload"), UNZIP(1, "Unzip"), READ(2, "Read"), IMPORT(3,
				"Import"), RESOLVE_DISTANCE(4, "Resolve distances"), DONE(5,
				"Done");

		private int stateNum;
		private String stateName;

		private JobState(int stateNum, String stateName) {
			this.stateNum = stateNum;
			this.stateName = stateName;
		}
	}
	
	private boolean failed;
	private JobFailure failure;

	public ScheduleImportJobState() {
		this.setStates(Arrays.asList(JobState.UPLOAD.stateName,
				JobState.UNZIP.stateName, JobState.READ.stateName,
				JobState.IMPORT.stateName, JobState.RESOLVE_DISTANCE.stateName,
				JobState.DONE.stateName));
		this.setType(JobType.SCHEDULE_IMPORT);
		this.setState(JobState.UPLOAD.stateNum);
		this.setStateProgress(-1.0f);
	}

	public void publishChange(ActorRef ar) {
		JobMonitoringActor.actor().tell(new JobProgressMessage(this), ar);
	}

	public void uploaded(ActorRef ar) {
		this.setState(JobState.UNZIP.stateNum);
		this.setStateProgress(-1.0f);
		this.publishChange(ar);
		
	}

	public void unzipped(ActorRef ar) {
		this.setState(JobState.READ.stateNum);
		this.setStateProgress(-1.0f);
		this.publishChange(ar);
	}

	public void read(ActorRef ar) {
		this.setState(JobState.IMPORT.stateNum);
		this.setStateProgress(-1.0f);
		this.publishChange(ar);
	}

	public void imported(ActorRef ar) {
		this.setState(JobState.RESOLVE_DISTANCE.stateNum);
		this.setStateProgress(0.0f);
		this.publishChange(ar);
	}

	public void distancesResolved(ActorRef ar) {
		this.setState(JobState.DONE.stateNum);
		this.setDone(true);
		this.publishChange(ar);
	}

	@Override
	public JsonNode toJsonNode() {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.valueToTree(this);
		return node;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	
	public void setFailure(JobFailure failure) {
		this.failure = failure;
		this.failed = true;
	}
}
