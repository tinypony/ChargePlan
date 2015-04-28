import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;

import org.emn.calculate.route.DailyConsumptionModel;
import org.junit.Before;
import org.junit.Test;

import utils.DateUtils;


public class DailyConsumptionModelTest {
	
	DailyConsumptionModel model;
	
	@Before
	public void setup() {
		Calendar cal = Calendar.getInstance();
		cal = DateUtils.rewindCalendar(cal);
		model = new DailyConsumptionModel();
		model.setDate(cal.getTime());
		
		HashMap<Long, Double> powerMap = new HashMap<Long, Double>();
		for(int i=0; i<60*24-1; i++) {
			powerMap.put(cal.getTime().getTime(), 100.0);
			cal.add(Calendar.MINUTE, 1);
		}
		
		model.setConsumptionMap(powerMap);
	}

	@Test
	public void returnsCorrectEnergyConsumption() {
		assertEquals(100.0, model.getHourlyEnergy(0), 0.1);
	}

}
