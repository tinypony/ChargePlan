package dto.message.client;

import java.util.List;

public class JobMonitoringRequest {

	String clientId;
	String action;
	List<String> jobs;
	
	public JobMonitoringRequest() {
		
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<String> getJobs() {
		return jobs;
	}

	public void setJobs(List<String> jobs) {
		this.jobs = jobs;
	}	
}
