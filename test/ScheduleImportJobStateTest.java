import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import actors.JobManagerActor;
import akka.actor.ActorRef;

import com.google.common.eventbus.Subscribe;

import dto.jobstate.ScheduleImportJobState;
import dto.message.JobProgressMessage;


public class ScheduleImportJobStateTest {

	ScheduleImportJobState state;
	
	private class JobProgressFollower {
		int upload = 0;
		int unzip = 0;
		int parsing = 0;
		int importing = 0;
		int distance = 0;
		int done = 0;
		public JobProgressFollower() {}
		
		@Subscribe
		public void onProgress(JobProgressMessage ev) {
			ScheduleImportJobState state = (ScheduleImportJobState) ev.getJobState();
			switch(state.getState()) {
				case 0: 
					upload++;
					break;
				case 1:
					unzip++;
					break;
				case 2:
					parsing++;
					break;
				case 3:
					importing++;
					break;
				case 4:
					distance++;
					break;
				case 5:
					done++;
					break;
				default:
					break;
						
			}
		}
	}
	
	JobProgressFollower follower;
	
	ActorRef ar;
	
	@Before
	public void init() {
		follower = new JobProgressFollower();
		state = JobManagerActor.createScheduleImportJobState();
		ar = Mockito.mock(ActorRef.class);
	}
	
	@Test
	public void testEventPropagation(){
		state.uploaded(ar);
		state.unzipped(ar);
		state.read(ar);
		state.imported(ar);
		state.imported(ar);
		state.distancesResolved(ar);
		
		assertEquals(follower.upload, 0);
		assertEquals(follower.unzip, 1);
		assertEquals(follower.parsing, 1);
		assertEquals(follower.importing, 1);
		assertEquals(follower.distance, 2);
		assertEquals(follower.done, 1);
	}
}
