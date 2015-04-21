import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import model.dataset.ScheduleStop;
import model.dataset.BusTrip;

import org.emn.plan.SimpleBusScheduler;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleSchedulerTest {

	SimpleBusScheduler scheduler;
	List<BusTrip> allTrips;
	
	@Before
	public void setup() {
		scheduler = new SimpleBusScheduler();
		allTrips = new ArrayList<BusTrip>();
		
		allTrips.add(makeBusTrip("0", "1000", "1100"));
		allTrips.add(makeBusTrip("0", "1005", "1105"));
		allTrips.add(makeBusTrip("1", "1105", "1205"));
		allTrips.add(makeBusTrip("1", "1115", "1215"));
		allTrips.add(makeBusTrip("1", "1145", "1245"));
		allTrips.add(makeBusTrip("1", "1215", "1315"));
		
	}
	
	public BusTrip makeBusTrip(String direction, String first, String last) {
		BusTrip trip1 = new BusTrip();
		trip1.setDirection(direction);
		
		ScheduleStop stop11 = new ScheduleStop();
		stop11.setArrival(first);
		
		ScheduleStop stop12 = new ScheduleStop();
		stop12.setArrival(last);
		
		trip1.addStop(stop11);
		trip1.addStop(stop12);
		
		return trip1;
	}
	
	@Test
	public void simpleTest() {
		scheduler.schedule(allTrips, 5);
		Queue<BusTrip> dirA = scheduler.getDirectionA();
		Queue<BusTrip> dirB = scheduler.getDirectionB();
		
		assertEquals("1000", dirA.poll().getStops().get(0).getArrival());
		assertEquals("1105", dirB.poll().getStops().get(0).getArrival());
		
		assertNull(dirA.poll());
		assertNull(dirB.poll());
	}
}
