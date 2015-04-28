import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.emn.calculate.EnergyPricingModel;
import org.emn.calculate.IEnergyPriceProvider;
import org.emn.calculate.route.DailyConsumptionModel;
import org.junit.Before;
import org.junit.Test;

import utils.DateUtils;


public class EnergyPricingModelTest {
	
	EnergyPricingModel model;
	DailyConsumptionModel consumptionModel;
	
	@Before
	public void setup() {
		model = new EnergyPricingModel(new StubEnergyPriceProvider());
		
		Calendar cal = Calendar.getInstance();
		cal = DateUtils.rewindCalendar(cal);
		consumptionModel = new DailyConsumptionModel();
		consumptionModel.setDate(cal.getTime());
		
		HashMap<Long, Double> powerMap = new HashMap<Long, Double>();
		for(int i=0; i<60*24-1; i++) {
			powerMap.put(cal.getTime().getTime(), 100.0);
			cal.add(Calendar.MINUTE, 1);
		}
		
		consumptionModel.setConsumptionMap(powerMap);
	}

	@Test
	public void calculatesCorrectlyTotalCost() {
		Double cost = model.getEnergyCost(Arrays.asList(consumptionModel));
		assertEquals(1030.9935484, cost, 0.1);
	}
	
	private class StubEnergyPriceProvider implements IEnergyPriceProvider {

		@Override
		public Double getMWhPrice(Date time) {
			// TODO Auto-generated method stub
			return 200.0;
		}
		
	}

}
