package dto.jobstate;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;

@Entity("jobs")
public abstract class JobState {
	
	private String id;
	private float totalProgress;
	private float stateProgress;
	private String type;
	private int state;
	private List<String> states;
	private boolean isDone;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public JsonNode toJsonNode() {
		return null;
	}
	public float getStateProgress() {
		return stateProgress;
	}

	public void setStateProgress(float stateProgress) {
		this.stateProgress = stateProgress;
	}

	public float getTotalProgress() {
		return totalProgress;
	}

	public void setTotalProgress(float totalProgress) {
		this.totalProgress = totalProgress;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}
}
