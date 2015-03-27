package dto.message;

import dto.jobstate.JobState;

public class JobProgressMessage {
	private JobState jobState;
	
	public JobProgressMessage(JobState state) {
		this.setJobState(state);
	}

	public JobState getJobState() {
		return jobState;
	}

	public void setJobState(JobState jobState) {
		this.jobState = jobState;
	}
}
