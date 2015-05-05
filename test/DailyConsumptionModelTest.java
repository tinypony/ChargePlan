import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emn.calculate.route.DailyConsumptionModel;
import org.emn.calculate.route.HourlyConsumptionEntry;
import org.junit.Before;
import org.junit.Test;

import utils.DateUtils;


public class DailyConsumptionModelTest {
	
	DailyConsumptionModel model;
	
	@Before
	public void setup() {
		Calendar cal = Calendar.getInstance();
		cal = DateUtils.rewindCalendar(cal);
		model = new DailyConsumptionModel(cal);
		Calendar cal1 = Calendar.getInstance();
		cal1 = DateUtils.stringToCalendar(cal1, "0030");
		model.consume("1", cal1, 600, 100.0);
		model.consume("2", cal1, 300, 400.0);
	
	}

	@Test
	public void returnsCorrectEnergyConsumption() {
		Double totalAvgPower0 = getTotalAvgPower(model.getHourlyEnergy(0));
		Double totalAvgPower1 = getTotalAvgPower(model.getHourlyEnergy(1));
		assertEquals(50.0, totalAvgPower0, 0.1);
		assertEquals(0.0, totalAvgPower1, 0.1);
	}
	
	public Double getTotalAvgPower(List<HourlyConsumptionEntry> entries) {
		Double totalAvgPower0 = 0.0;
		
		for(HourlyConsumptionEntry ent: entries) {
			totalAvgPower0 += ent.getAvgPower();
		}
		return totalAvgPower0;
	}
	
	@Test
	public void returnsCorrectHourlyBreakdown() {
		Map<Integer, List<HourlyConsumptionEntry>> hourly = model.getHourlyConsumptionDistribution();
		assertEquals(2, hourly.get(0).size());
		assertEquals(2, hourly.get(1).size());
		
		assertEquals(50.0, getTotalAvgPower(hourly.get(0)), 0.1);
		assertEquals(0.0, getTotalAvgPower(hourly.get(1)), 0.1);
		
	}

}
