import model.dataset.BusTrip;
import model.dataset.ScheduleStop;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class BusTripTest {

	BusTrip tripA;
	BusTrip tripB;
	
	@Before
	public void setup() {
		tripA = new BusTrip();
		tripB = new BusTrip();
		
		ScheduleStop s1 = new ScheduleStop();
		s1.setArrival("1000");
		tripA.addStop(s1);
		
		ScheduleStop s2 = new ScheduleStop();
		s2.setArrival("1001");
		tripB.addStop(s2);
	}
	
	@Test
	public void aShouldBeLesserThanB() {
		assertEquals(-1, tripA.compareTo(tripB));
	}
}
