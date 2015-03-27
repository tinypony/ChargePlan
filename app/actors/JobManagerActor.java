package actors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import play.libs.Akka;
import play.mvc.Http.MultipartFormData;
import actors.jobs.ScheduleImportJob;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import utils.MongoUtils;
import dto.jobstate.JobState;
import dto.jobstate.ScheduleImportJobState;
import dto.message.JobSpawnMessage;
import dto.message.ScheduleImportJobCreationRequest;
import dto.message.StartImportMessage;

/**
 * Singleton class that keeps track of active jobs and stores 
 * communication channels to allow for bidirectional communication
 * between a client and the running jobs
 * @author tinypony
 *
 */
public class JobManagerActor extends UntypedActor {

	private static ActorRef actor = Akka.system().actorOf(Props.create(JobManagerActor.class));
	
	
	private List<ActorRef> spawnedJobs;
	private JobManagerActor() {
		this.spawnedJobs = new ArrayList<ActorRef>();
	}
	
	private static String generateId() {
		return UUID.randomUUID().toString();
	}
	
	
	public static ScheduleImportJobState createScheduleImportJobState() {
		String newId = generateId();
		ScheduleImportJobState jobState = new ScheduleImportJobState();
		jobState.setId(newId);
		return jobState;
	}
	
	public void createScheduleImportJob(ScheduleImportJobState state, MultipartFormData body) {
		Props props = ScheduleImportJob.props(state, body);
		ActorRef actref = this.getContext().actorOf(props);
		this.getContext().watch(actref);
		actref.tell(new StartImportMessage(), null);
		this.addJob(actref, state);

	}
	
	public void addJob(ActorRef job, JobState state) {
		this.spawnedJobs.add(job);
		//tell monitor to include job as active
		JobMonitoringActor.actor().tell(new JobSpawnMessage(state), getSelf());
	}
	
	public List<ActorRef> getActiveJobs() {
		return this.spawnedJobs;
	}
	
	public void removeJob(String jobId) {
	}
	
//	public void pauseJob(String jobId) throws InterruptedException {
//		this.getChannel(jobId).post(new JobControlEvent(jobId, JobControlEvent.Type.PAUSE));
//	}
//	
//	public void unpauseJob(String jobId) throws InterruptedException {
//		this.getChannel(jobId).post(new JobControlEvent(jobId, JobControlEvent.Type.UNPAUSE));
//	}
//	
//	public void stopJob(String jobId) throws InterruptedException {
//		this.getChannel(jobId).post(new JobControlEvent(jobId, JobControlEvent.Type.STOP));
//	}
	
	public static ActorRef actor() {
		return actor;
	}
	@Override
	public void onReceive(Object msg) throws Exception {
		if(msg instanceof ScheduleImportJobCreationRequest) {
			ScheduleImportJobCreationRequest typed = (ScheduleImportJobCreationRequest) msg;
			this.createScheduleImportJob(typed.getJobState(), typed.getBody());
		}
		
	}
}
