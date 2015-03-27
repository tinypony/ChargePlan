package dto.message;

import dto.jobstate.JobState;

public class JobSpawnMessage {
	
	JobState state;
	
	public JobSpawnMessage(JobState s) {
		state = s;
	}
	
	public JobState getState() {
		return this.state;
	}

}
