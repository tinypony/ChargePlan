package actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dto.jobstate.JobState;
import dto.message.client.JobMonitoringResponse;
import play.Logger;
import play.mvc.WebSocket;


public class JobMonitoringActorBrain {
	List<JobState> runningJobs;
	Map<String, WebSocket.Out<JsonNode>> registered; //<ClientId, ClientSocket>
	Map<String, Set<WebSocket.Out<JsonNode>>> monitoring; //<JobId, Set<ClientSocket>>
	Map<String, Set<String>> monitoredJobs; // <ClientId, Set<JobId>>
	
	public JobMonitoringActorBrain() {
		runningJobs = new ArrayList<JobState>();
		registered = new HashMap<String, WebSocket.Out<JsonNode>>();
    	monitoring = new HashMap<String, Set<WebSocket.Out<JsonNode>>>();
    	monitoredJobs = new HashMap<String, Set<String>>();
	}
	
	public void addRunningJob(JobState s) {
		Logger.info("Added new running job, id = {}", s.getId());
		runningJobs.add(s);
		ObjectMapper om = new ObjectMapper();
		JobMonitoringResponse resp = new JobMonitoringResponse();
		resp.setMessageType("newJob");
		resp.setJob(s);
		this.broadcastAll(om.valueToTree(resp));
	}
	
    private void broadcastAll(JsonNode payload) {
		for(WebSocket.Out<JsonNode> out: registered.values()) {
			out.write(payload);
		}		
	}

	public void registerClient(String id, WebSocket.Out<JsonNode> out) {
    	if(this.registered.containsKey(id)) {
    		throw new IllegalStateException("Client already registered");
    	}
    	this.registered.put(id, out);
    	this.monitoredJobs.put(id, new HashSet<String>());
    }
    
    public void unregisterClient(String clientId) {
    	WebSocket.Out<JsonNode> clientOut = this.registered.remove(clientId);
    	Set<String> monitoredJobs = this.monitoredJobs.get(clientId);
    	
    	for(String job: monitoredJobs) {
    		Set<WebSocket.Out<JsonNode>> jobClients = this.monitoring.get(job);
    		jobClients.remove(clientOut);
    	}
    	
    	this.monitoredJobs.remove(clientId);
    }
    
    public void monitor(String id, String jobId) {
    	WebSocket.Out<JsonNode> clientOut = this.registered.get(id);
    	
    	if(clientOut == null) {
    		throw new IllegalStateException("Client is not registered");
    	}
    	
    	Set<WebSocket.Out<JsonNode>> socketsMonitoring = monitoring.get(jobId);
  
    	if(socketsMonitoring == null) {
    		socketsMonitoring = new HashSet<WebSocket.Out<JsonNode>>();
    		monitoring.put(jobId, socketsMonitoring);
    	}
    	
    	this.monitoredJobs.get(id).add(jobId);
    	socketsMonitoring.add(clientOut);
    	
    }
    
    public void finish(JobState s) {
    	Logger.info("Running jobs contain finishing job: {}", this.runningJobs.contains(s));
    	this.runningJobs.remove(s);
    	this.monitoring.remove(s.getId());
    	for(String client: registered.keySet()) {
    		this.getMonitoredJobs(client).remove(s.getId());
    	}
    }
    
	public Map<String, WebSocket.Out<JsonNode>> getRegisteredChannels() {
		return registered;
	}
	
	public WebSocket.Out<JsonNode> getRegisteredChannel(String clientId) {
		return registered.get(clientId);
	}

	public Map<String, Set<WebSocket.Out<JsonNode>>> getMonitoringChannels() {
		return monitoring;
	}
	
	public Set<WebSocket.Out<JsonNode>> getMonitoringChannels(String jobId) {
		return monitoring.get(jobId);
	}

	public Map<String, Set<String>> getMonitoredJobs() {
		return monitoredJobs;
	}
	
	public Set<String> getMonitoredJobs(String clientId) {
		return monitoredJobs.get(clientId);
	}
	
	public List<JobState> getRunninJobs() {
		return this.runningJobs;
	}

	public void broadcast(String jobId, JsonNode payload) {
		Set<WebSocket.Out<JsonNode>> channels = getMonitoringChannels(jobId);
		if(channels == null) {
			return;
		}
		
		for(WebSocket.Out<JsonNode> channel : channels) {
			Logger.info("Broadcast {} state", jobId);
			channel.write(payload);
		}
	}

	public void listJobsTo(String clientId) {
		ObjectMapper om = new ObjectMapper();		
		JobMonitoringResponse resp = new JobMonitoringResponse();
		resp.setMessageType("activeJobs");
		resp.setJobs(runningJobs);
		this.getRegisteredChannel(clientId).write(om.valueToTree(resp));		
	}
}
