import static org.junit.Assert.*;

import org.emn.calculate.route.ChargingEvent;
import org.junit.Test;


public class ChargingEventTest {

	@Test
	public void mustProduceCorrectEnergyConsumedEventHours() {
		ChargingEvent event = new ChargingEvent();
		event.setDuration(3600);
		event.setPower(100.0);
		
		assertEquals(100.0, event.getPowerConsumed(), 0.1);
	}
	
	@Test
	public void mustProduceCorrectEvenrgyConsumedFractionHours() {
		ChargingEvent event = new ChargingEvent();
		event.setDuration(1800);
		event.setPower(100.0);
		
		assertEquals(50.0, event.getPowerConsumed(), 0.1);
	}
}
