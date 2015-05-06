import static org.junit.Assert.*;

import org.emn.plan.model.BusInstance;
import org.emn.plan.model.ElectricBus;
import org.junit.Before;
import org.junit.Test;


public class BusInstaceTest {

	BusInstance bus;
	
	@Before
	public void setup() {
		ElectricBus testbus = new ElectricBus();
		testbus.setCapacity(100);
		bus = new BusInstance(testbus);
	}
	
	@Test
	public void mustCorrectlyDecreaseBatteryCharge() {
		assertEquals(99, bus.drive(1000, 1.0), 0.01);
	}
	
	@Test(expected=IllegalStateException.class)
	public void mustCorrectlyDecreaseBatteryChargeToZero() {
		assertEquals(0, bus.drive(1000000, 1.0), 0.1);
	}
	
	@Test
	public void mustCorrectlyRegenerateBatteryChargeToHundred() {
		bus.drive(10000, 1.0);
		assertEquals(90, bus.getBatteryState(), 0.1);
		assertEquals(100, bus.drive(10000, -1.0), 0.1);
	}
	
	@Test
	public void mustCorrectlyDetermineTimeSpentCharging() {
		assertEquals(0, bus.charge(1000, 100));
		
		bus.drive(10000, 2.0);
		assertEquals(80, bus.getBatteryState(), 0.1);
		assertEquals(10, bus.charge(10, 200));
		assertEquals(80.5556, bus.getBatteryState(), 0.1);
	}
}
