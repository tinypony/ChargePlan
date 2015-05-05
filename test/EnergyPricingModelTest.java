import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.emn.calculate.EnergyPricingModel;
import org.emn.calculate.IEnergyPriceProvider;
import org.emn.calculate.route.DailyConsumptionModel;
import org.junit.Before;
import org.junit.Test;

import utils.DateUtils;


public class EnergyPricingModelTest {
	
	EnergyPricingModel model;
	DailyConsumptionModel consumptionModel;
	List<DailyConsumptionModel> history;
	
	@Before
	public void setup() {
		model = new EnergyPricingModel(new StubEnergyPriceProvider());
		
		Calendar cal = Calendar.getInstance();
		cal = DateUtils.rewindCalendar(cal);
		
		consumptionModel = new DailyConsumptionModel(cal);
		cal = DateUtils.stringToCalendar(cal, "0030");
		consumptionModel.consume("1", cal, 600, 100);
		
		cal = DateUtils.stringToCalendar(cal, "0040");
		consumptionModel.consume("2", cal, 400, 100);
		
		history = Arrays.asList(consumptionModel);
	}
	
	@Test
	public void calculatesPeakPower() {
		assertEquals(27.777778, model.getHistoryPeakPower(history), 0.1);
	}
	
	@Test
	public void calculatesHourlyConsumption() {
		assertEquals(27.77778, model.getTotalEnergy(consumptionModel.getHourlyEnergy(0)), 0.1);
		assertEquals(16.66667, model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "1"), 0.1);
	}
	
	@Test 
	public void calculatesCostOfProduction() {
		assertEquals(5.555555, model.getHourlyProductionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0))
				), 0.1);
		
		assertEquals(3.333333, model.getHourlyProductionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "1")
				), 0.1);
		
		assertEquals(2.222222, model.getHourlyProductionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "2")
				), 0.1);
		
		
	}
	
	@Test 
	public void calculatesCostOfTransmission() {
		assertEquals(1.5833, model.getHourlyTransmissionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0))
				), 0.1);
		
		assertEquals(0.95, model.getHourlyTransmissionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "1")
				), 0.1);
		
		assertEquals(0.633333, model.getHourlyTransmissionCost(
				Calendar.getInstance().getTime(), 
				model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "2")
				), 0.1);
	}
	
	@Test 
	public void calculatesPeakPowerCost() {
		assertEquals(112.00716, model.getPeakPowerCost(history, null), 0.1);
		assertEquals(67.2042961, model.getPeakPowerCost(history, "1"), 0.1);
	}

	@Test
	public void calculatesCorrectlyTotalCost() {
		Double cost = model.getEnergyCost(Arrays.asList(consumptionModel), null);
		assertEquals(130.113757, cost, 0.1);
	}
	

	@Test
	public void calculatesCorrectlyTotalEnergy() {
		Double tot = model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), null);
		assertEquals(27.7777, tot, 0.1);
		tot = model.getTotalEnergy(consumptionModel.getHourlyEnergy(0), "1");
		assertEquals(16.6667, tot, 0.1);
	}
	
	@Test
	public void allocatesCorrectlyRouteCost() {
		Double cost = model.getEnergyCost(Arrays.asList(consumptionModel), "1");
		Double cost1 = model.getEnergyCost(Arrays.asList(consumptionModel), "2");


		assertEquals(82.45537, cost, 0.1);
		assertEquals(58.626161, cost1, 0.1);
	}
	
	private class StubEnergyPriceProvider implements IEnergyPriceProvider {

		@Override
		public Double getMWhPrice(Date time) {
			// TODO Auto-generated method stub
			return 200.0;
		}
		
	}

}
