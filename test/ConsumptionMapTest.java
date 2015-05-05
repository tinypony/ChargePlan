import static org.junit.Assert.*;

import java.util.Calendar;

import org.emn.calculate.route.ConsumptionMap;
import org.junit.Test;


public class ConsumptionMapTest {

	@Test
	public void testSimple() {
		Calendar cal = Calendar.getInstance();
		ConsumptionMap map = new ConsumptionMap(cal);
		map.addChargingEvent(0, 0, 3600, 100.0);
		assertEquals(100.0, map.getHourlyAveragePower(0), 0.1);
		assertEquals(0.0, map.getHourlyAveragePower(2), 0.1);
	}

	@Test
	public void testSimple2() {
		Calendar cal = Calendar.getInstance();
		ConsumptionMap map = new ConsumptionMap(cal);
		map.addChargingEvent(0, 0, 1800, 100.0);
		map.addChargingEvent(0, 30, 1800, 50.0);
		assertEquals(75.0, map.getHourlyAveragePower(0), 0.1);
	}
	
	@Test
	public void testSimpleOverHour() {
		Calendar cal = Calendar.getInstance();
		ConsumptionMap map = new ConsumptionMap(cal);
		map.addChargingEvent(0, 30, 3600, 100.0);
		assertEquals(50.0, map.getHourlyAveragePower(1), 0.1);
		assertEquals(50.0, map.getHourlyAveragePower(0), 0.1);
		
	}
}
