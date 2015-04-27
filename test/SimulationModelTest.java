import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import model.dataset.BusTrip;
import model.dataset.ScheduleStop;
import model.planning.BusChargerInstance;
import model.planning.BusInstance;
import model.planning.ElectrifiedBusStop;
import model.planning.solutions.BusCharger;
import model.planning.solutions.ElectricBus;

import org.emn.calculate.bus.StaticConsumptionProfile;
import org.emn.calculate.route.RouteSimulationModel;
import org.emn.plan.SimulationResult;
import org.junit.Before;
import org.junit.Test;

import configuration.emn.route.StopsDistanceRetriever;


public class SimulationModelTest {
	
	RouteSimulationModel model;
	BusInstance bus;
	Queue<BusTrip> directionA;
	Queue<BusTrip> directionB;
	Queue<BusTrip> directionC;
	
	List<ElectrifiedBusStop> electrifiedStops;
	StaticConsumptionProfile profile;
	
	
	public class StubDistanceRetriever implements StopsDistanceRetriever {

		@Override
		public int getDistanceBetweenStops(ScheduleStop a, ScheduleStop b)
				throws Exception {
			return 1000;
		}
	}
	
	@Before
	public void setUp() throws Exception {
		ElectricBus bustype = new ElectricBus();
		bustype.setCapacity(100);
		bus = new BusInstance(bustype);
		
		electrifiedStops = new ArrayList<ElectrifiedBusStop>();
		setupChargers();
		
		directionA = new LinkedBlockingQueue<BusTrip>();
		directionB = new LinkedBlockingQueue<BusTrip>();
		directionC = new LinkedBlockingQueue<BusTrip>();
		
		setupDirectionA();
		
		profile = new StaticConsumptionProfile();
		profile.setConsumption(2.0);
		model = new RouteSimulationModel(profile, bus, "2015-05-01");
		model.setDistanceManager(new StubDistanceRetriever());
		model.setDirections(directionA, directionB);
		model.setElectrifiedStops(electrifiedStops);
	}

	@Test
	public void testSimpleSuccessfulTrip() throws Exception {
		SimulationResult result = model.simulate();
		assertTrue(result.isSurvived());
		assertEquals("1000", result.getBatteryHistory().get(0).getSimpleTimestamp());
		assertEquals(100, result.getBatteryHistory().get(0).getCharge(), 0.1);

		assertEquals("1010", result.getBatteryHistory().get(1).getSimpleTimestamp());
		assertEquals(98, result.getBatteryHistory().get(1).getCharge(), 0.1);
		
		assertEquals("1020", result.getBatteryHistory().get(2).getSimpleTimestamp());
		assertEquals(96, result.getBatteryHistory().get(2).getCharge(), 0.1);
		
		assertEquals("1020", result.getBatteryHistory().get(3).getSimpleTimestamp());
		assertEquals(97.6666, result.getBatteryHistory().get(3).getCharge(), 0.1);
	}
	
	@Test
	public void testRoundSuccessfulTrip() throws Exception {
		setupDirectionB();
		
		SimulationResult result = model.simulate();
		assertTrue(result.isSurvived());
		
		assertEquals("1000", result.getBatteryHistory().get(0).getSimpleTimestamp());
		assertEquals(100, result.getBatteryHistory().get(0).getCharge(), 0.1);

		assertEquals("1010", result.getBatteryHistory().get(1).getSimpleTimestamp());
		assertEquals(98, result.getBatteryHistory().get(1).getCharge(), 0.1);
		
		assertEquals("1020", result.getBatteryHistory().get(2).getSimpleTimestamp());
		assertEquals(96, result.getBatteryHistory().get(2).getCharge(), 0.1);
		
		//charge
		assertEquals("1020", result.getBatteryHistory().get(3).getSimpleTimestamp());
		assertEquals("3", result.getBatteryHistory().get(3).getLocation());
		assertEquals(97.6666, result.getBatteryHistory().get(3).getCharge(), 0.1);
		
		//return trip
		assertEquals("1021", result.getBatteryHistory().get(4).getSimpleTimestamp());
		assertEquals("3", result.getBatteryHistory().get(4).getLocation());
		assertEquals(97.6666, result.getBatteryHistory().get(4).getCharge(), 0.1);

		assertEquals("1031", result.getBatteryHistory().get(5).getSimpleTimestamp());
		assertEquals(95.6666, result.getBatteryHistory().get(5).getCharge(), 0.1);
		
		assertEquals("1041", result.getBatteryHistory().get(6).getSimpleTimestamp());
		assertEquals("1", result.getBatteryHistory().get(6).getLocation());
		assertEquals(93.6666, result.getBatteryHistory().get(6).getCharge(), 0.1);
		
		//charge
		assertEquals("1042", result.getBatteryHistory().get(7).getSimpleTimestamp());
		assertEquals("1", result.getBatteryHistory().get(7).getLocation());
		assertEquals(97, result.getBatteryHistory().get(7).getCharge(), 0.1);
	}
	
	@Test
	public void testSimpleFailedTrip() throws Exception {
		bus.drive(990, 100);
		model.setBus(bus);
		SimulationResult result = model.simulate();
		assertTrue(!result.isSurvived());
		
		assertEquals("1000", result.getBatteryHistory().get(0).getSimpleTimestamp());
		assertEquals(1, result.getBatteryHistory().get(0).getCharge(), 0.1);

		assertEquals("1010", result.getBatteryHistory().get(1).getSimpleTimestamp());
		assertEquals(0, result.getBatteryHistory().get(1).getCharge(), 0.1);
	}
	
	@Test
	public void testSimpleOverMidnightTrip() throws Exception {
		this.setupDirectionC();
		model.setBus(bus);
		model.setDirectionA(directionC);
		SimulationResult result = model.simulate();
		assertTrue(result.isSurvived());
		Calendar cal = Calendar.getInstance();
		
		assertEquals("2340", result.getBatteryHistory().get(0).getSimpleTimestamp());
		cal.setTime(result.getBatteryHistory().get(0).getTimestamp());
		assertEquals(1, cal.get(Calendar.DATE));
		assertEquals(100, result.getBatteryHistory().get(0).getCharge(), 0.1);

		assertEquals("0000", result.getBatteryHistory().get(1).getSimpleTimestamp());
		cal.setTime(result.getBatteryHistory().get(1).getTimestamp());
		assertEquals(2, cal.get(Calendar.DATE));
		assertEquals(98, result.getBatteryHistory().get(1).getCharge(), 0.1);
		
		assertEquals("0010", result.getBatteryHistory().get(2).getSimpleTimestamp());
		cal.setTime(result.getBatteryHistory().get(2).getTimestamp());
		assertEquals(2, cal.get(Calendar.DATE));
		assertEquals(96, result.getBatteryHistory().get(2).getCharge(), 0.1);
	}
	
	private void setupDirectionA() {
		BusTrip trip1 = new BusTrip();
		
		ScheduleStop firstStop = new ScheduleStop();
		firstStop.setArrival("1000");
		firstStop.setOrder(1);
		firstStop.setStopId("1");
		
		ScheduleStop secondStop = new ScheduleStop();
		secondStop.setArrival("1010");
		secondStop.setOrder(2);
		secondStop.setStopId("2");
		
		ScheduleStop thirdStop = new ScheduleStop();
		thirdStop.setArrival("1020");
		thirdStop.setOrder(3);
		thirdStop.setStopId("3");
		
		trip1.addStop(firstStop);
		trip1.addStop(secondStop);
		trip1.addStop(thirdStop);
		
		trip1.setRouteId("route-1");
		directionA.add(trip1);
	}
	
	private void setupDirectionB() {
		BusTrip trip2 = new BusTrip();
		
		ScheduleStop firstStop = new ScheduleStop();
		firstStop.setArrival("1021");
		firstStop.setOrder(1);
		firstStop.setStopId("3");
		
		ScheduleStop secondStop = new ScheduleStop();
		secondStop.setArrival("1031");
		secondStop.setOrder(2);
		secondStop.setStopId("4");
		
		ScheduleStop thirdStop = new ScheduleStop();
		thirdStop.setArrival("1041");
		thirdStop.setOrder(3);
		thirdStop.setStopId("1");
		
		trip2.addStop(firstStop);
		trip2.addStop(secondStop);
		trip2.addStop(thirdStop);
		
		trip2.setRouteId("route-1");
		directionB.add(trip2);
	}
	
	private void setupDirectionC() {
		BusTrip trip2 = new BusTrip();
		
		ScheduleStop firstStop = new ScheduleStop();
		firstStop.setArrival("2340");
		firstStop.setOrder(1);
		firstStop.setStopId("1");
		
		ScheduleStop secondStop = new ScheduleStop();
		secondStop.setArrival("0000");
		secondStop.setOrder(2);
		secondStop.setStopId("2");
		
		ScheduleStop thirdStop = new ScheduleStop();
		thirdStop.setArrival("0010");
		thirdStop.setOrder(3);
		thirdStop.setStopId("3");
		
		trip2.addStop(firstStop);
		trip2.addStop(secondStop);
		trip2.addStop(thirdStop);
		
		trip2.setRouteId("route-1");
		directionC.add(trip2);
	}
	
	private void setupChargers() {
		ElectrifiedBusStop stop1 = new ElectrifiedBusStop();
		stop1.setStopId("1");
		BusCharger chargerType1 = new BusCharger();
		chargerType1.setPower(200);
		BusChargerInstance chargerInst1 = new BusChargerInstance();
		chargerInst1.setType(chargerType1);
		stop1.addCharger(chargerInst1);
		stop1.getChargingTimes().put("route-1", 60);
		
		ElectrifiedBusStop stop2 = new ElectrifiedBusStop();
		stop2.setStopId("3");
		BusCharger chargerType2 = new BusCharger();
		chargerType2.setPower(200);
		BusChargerInstance chargerInst2 = new BusChargerInstance();
		chargerInst2.setType(chargerType2);
		stop2.addCharger(chargerInst2);
		stop2.getChargingTimes().put("route-1", 30);
		electrifiedStops.add(stop1);
		electrifiedStops.add(stop2);
	}

}
