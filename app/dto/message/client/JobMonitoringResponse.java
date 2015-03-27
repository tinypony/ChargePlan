package dto.message.client;

import java.util.List;

import dto.jobstate.JobState;

public class JobMonitoringResponse {
	String messageType;
	JobState job;
	List<JobState> jobs;
	
	public JobMonitoringResponse() {
		
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public JobState getJob() {
		return job;
	}

	public void setJob(JobState job) {
		this.job = job;
	}

	public List<JobState> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobState> jobs) {
		this.jobs = jobs;
	}
	
	
}
