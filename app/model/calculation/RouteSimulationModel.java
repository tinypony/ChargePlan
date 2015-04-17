package model.calculation;

import java.util.List;
import java.util.Queue;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import model.dataset.BusTrip;
import model.dataset.ScheduleStop;
import model.dataset.StopDistance;
import model.planning.BusInstance;
import model.planning.ElectrifiedBusStop;
import model.planning.solutions.BusCharger;
import model.planning.solutions.ElectricBus;
import model.planning.solutions.IConsumptionProfile;

public class RouteSimulationModel {

	private Queue<BusTrip> directionA;
	private Queue<BusTrip> directionB;
	private List<ElectrifiedBusStop> electrifiedStops;
	private List<StopDistance> distances;
	private BusInstance bus;
	private IConsumptionProfile consumptionProfile;
	
	public RouteSimulationModel(IConsumptionProfile profile, BusInstance bus) {
		this.consumptionProfile = profile;
		this.bus = bus;
	}
	
	public void setDirections(Queue<BusTrip> dirA, Queue<BusTrip> dirB) {
		this.directionA = dirA;
		this.directionB = dirB;
	}

	public Queue<BusTrip> getDirectionA() {
		return directionA;
	}

	public void setDirectionA(Queue<BusTrip> directionA) {
		this.directionA = directionA;
	}

	public Queue<BusTrip> getDirectionB() {
		return directionB;
	}

	public void setDirectionB(Queue<BusTrip> directionB) {
		this.directionB = directionB;
	}

	public List<ElectrifiedBusStop> getElectrifiedStops() {
		return electrifiedStops;
	}

	public void setElectrifiedStops(List<ElectrifiedBusStop> electrifiedStops) {
		this.electrifiedStops = electrifiedStops;
	}

	public List<StopDistance> getDistances() {
		return distances;
	}

	public void setDistances(List<StopDistance> distances) {
		this.distances = distances;
	}

	
	public BusInstance getBus() {
		return bus;
	}

	public void setBus(BusInstance bus) {
		this.bus = bus;
	}

	public IConsumptionProfile getConsumptionProfile() {
		return consumptionProfile;
	}

	public void setConsumptionProfile(IConsumptionProfile consumptionProfile) {
		this.consumptionProfile = consumptionProfile;
	}

	public boolean simulate() {
		boolean canRun = true;
		Queue<BusTrip> direction = this.getDirectionA();
		SimulationResult result = new SimulationResult();
		
		ScheduleStop previousStop = null;
		ScheduleStop currentStop = null;
		boolean endStop = true;
		
		while(canRun) {
			BusTrip trip = direction.poll();
			
			if(trip == null) {
				canRun = false;
				break;
			}
			
			List<ScheduleStop> tripStops = trip.getStops();
			
			for(int i=0; i<tripStops.size(); i++) {
				if(i != 0) {
					previousStop = currentStop;
				}

				currentStop = tripStops.get(i);
				
				if(currentStop!=null && previousStop != null) {
					int meters = this.getDistance(previousStop, currentStop);
					double consumption = this.consumptionProfile.getConsumption(bus.getType(), null);
					double batteryState = this.bus.drive(meters, consumption);
					String arrivalTime = currentStop.getArrival();
					result.addBatteryStateEntry(new BatteryStateEntry(batteryState, arrivalTime));
					
				}
				
				ElectrifiedBusStop elStop = this.getElectrified(currentStop.getStopId());
				
				if(elStop != null) {
					int chargingTimeSeconds = this.getChargingTime(elStop);
					BusCharger chargerType = elStop.getCharger(currentStop.getArrival(), chargingTimeSeconds).getType();
					this.bus.charge(chargingTimeSeconds, chargerType.getPower());
				}
				
			}			
		}
		
		return true;
	}
	
	private int getChargingTime(ElectrifiedBusStop elStop) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Returns distance between two consecutive bus stops in meters
	 * @param previousStop
	 * @param currentStop
	 * @return
	 */
	private int getDistance(ScheduleStop previousStop, ScheduleStop currentStop) {
		return 500;
	}

	//Called often, potential optimization place
	public ElectrifiedBusStop getElectrified(final String stopId) {
		return Iterables.find(this.getElectrifiedStops(), new Predicate<ElectrifiedBusStop>() {

			@Override
			public boolean apply(ElectrifiedBusStop arg0) {
				return stopId.equals(arg0.getStopId());
			}
			
		}, null);
	}
}
