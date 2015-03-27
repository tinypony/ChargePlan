import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.mockito.Mock;
import org.mockito.Mockito;

import actors.JobManagerActor;
import actors.JobMonitoringActor;
import actors.JobMonitoringActorBrain;

import com.fasterxml.jackson.databind.JsonNode;

import dto.jobstate.ScheduleImportJobState;
import play.mvc.WebSocket;


public class JobMonitoringActorBrainTest {

	@Mock
	WebSocket.Out<JsonNode> outConn;
	
	JobMonitoringActorBrain brain;
	ScheduleImportJobState state;
	
	@Before
	public void setup() {
		brain = new JobMonitoringActorBrain();
		outConn = Mockito.mock(WebSocket.Out.class);
		state = new ScheduleImportJobState();
		state.setId("123");
	}
	
	@Test
	public void monitoredJobsMustBeEmptyAfterRegister() {
		String myId = "1";
		brain.registerClient(myId, outConn);
		assertEquals(1, brain.getRegisteredChannels().size());
		assertEquals(0, brain.getMonitoredJobs().get(myId).size());
		assertEquals(0, brain.getMonitoringChannels().size());
	}
	
	@Test
	public void monitorCallMustAddChannelAndJob() {
		String myId = "1";
		String jobId = "23";
		brain.registerClient(myId, outConn);
		brain.monitor(myId, jobId);
		
		assertEquals(1, brain.getRegisteredChannels().size());

		assertEquals(1, brain.getMonitoredJobs().size());
		assertEquals(1, brain.getMonitoredJobs(myId).size());
		assertEquals(1, brain.getMonitoringChannels().size());
		assertEquals(1, brain.getMonitoringChannels(jobId).size());
		
	}
	

	
	@Test
	public void callingMonitorTwiceWithSameParamsHasNoEffect() {
		String myId = "1";
		String jobId = "23";
		brain.registerClient(myId, outConn);
		brain.monitor(myId, jobId);
		brain.monitor(myId, jobId);
		brain.monitor(myId, jobId);
		
		assertEquals(1, brain.getRegisteredChannels().size());

		assertEquals(1, brain.getMonitoredJobs().size());
		assertEquals(1, brain.getMonitoredJobs(myId).size());
		assertEquals(1, brain.getMonitoringChannels().size());
		assertEquals(1, brain.getMonitoringChannels(jobId).size());
	}
	
	@Test(expected=IllegalStateException.class)
	public void cannotMonitorJobForUnregisteredClient() {
		brain.monitor("234234", "234");
	}
	
	@Test(expected=IllegalStateException.class)
	public void cannotRegisterSameIdTwice() {
		String myId = "1";
		brain.registerClient(myId, outConn);
		brain.registerClient(myId, outConn);
	}
	
	@Test
	public void cleansUpCorrectlyAfterClientUnregisters() {
		String myId = "1";
		String jobId = "23";
		
		brain.registerClient(myId, outConn);
		brain.monitor(myId, jobId);
		
		brain.unregisterClient(myId);
		assertEquals(0, brain.getMonitoredJobs().size());
		assertEquals(0, brain.getRegisteredChannels().size());
		assertFalse(brain.getMonitoredJobs().containsKey(myId));
		assertEquals(1, brain.getMonitoringChannels().size());
		assertEquals(0, brain.getMonitoringChannels(jobId).size());
	}
	
	@Test
	public void cleansUpCorrectlyAfterJobFinishes(){
		brain.addRunningJob(state);
		brain.registerClient("1", outConn);
		brain.monitor("1", state.getId());
		assertEquals(1, brain.getRunninJobs().size());
		assertEquals(1, brain.getMonitoredJobs("1").size());
		brain.finish(state);
		assertEquals(0, brain.getRunninJobs().size());
		assertEquals(0, brain.getMonitoredJobs("1").size());
	}
	
	
	
}
