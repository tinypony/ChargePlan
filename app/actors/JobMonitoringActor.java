package actors;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.message.ClientRegistrationMessage;
import dto.message.ClientUnregistrationMessage;
import dto.message.JobProgressMessage;
import dto.message.JobSpawnMessage;
import dto.message.client.JobMonitoringRequest;
import dto.message.client.JobMonitoringResponse;
import play.Logger;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class JobMonitoringActor extends UntypedActor {

	public static ActorRef actor = Akka.system().actorOf(
			Props.create(JobMonitoringActor.class));

	JobMonitoringActorBrain brain;

	public JobMonitoringActor() {
		brain = new JobMonitoringActorBrain();
	}

	public static ActorRef actor() {
		return actor;
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof ClientRegistrationMessage) {
			ClientRegistrationMessage typedMsg = (ClientRegistrationMessage) msg;
			this.brain.registerClient(typedMsg.id, typedMsg.channel);
			
		} else if(msg instanceof ClientUnregistrationMessage){
			String clientId = ((ClientUnregistrationMessage) msg).getClientId();
			this.brain.unregisterClient(clientId);
			
		} else if (msg instanceof JobProgressMessage) {
			JobProgressMessage typedMsg = (JobProgressMessage) msg;
			JobMonitoringResponse resp = new JobMonitoringResponse();
			resp.setMessageType("jobProgress");
			resp.setJob(typedMsg.getJobState());
			
			this.brain.broadcast(typedMsg.getJobState().getId(), new ObjectMapper().valueToTree(resp));
			if(typedMsg.getJobState().isDone()) {
				this.brain.finish(typedMsg.getJobState());
			}
			
		} else if (msg instanceof JobSpawnMessage) {
			JobSpawnMessage typedMsg = (JobSpawnMessage) msg;
			this.brain.addRunningJob(typedMsg.getState());
			
		} else if (msg instanceof JobMonitoringRequest) {
			JobMonitoringRequest typedMsg = (JobMonitoringRequest) msg;
			
			if ("listJobs".equals(typedMsg.getAction())) {
				this.brain.listJobsTo(typedMsg.getClientId());
			} else if ("monitor".equals(typedMsg.getAction())) {
				
				for(String j: typedMsg.getJobs()) {
					brain.monitor(typedMsg.getClientId(), j);
				}
			}
			
		} else {
			unhandled(msg);
		}
	}
}
