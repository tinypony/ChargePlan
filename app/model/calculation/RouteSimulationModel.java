package model.calculation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;

import scala.util.control.Exception.Finally;

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

	public SimulationResult simulate() {
		boolean canRun = true;
		Queue<BusTrip> direction = this.getDirectionA();
		SimulationResult result = new SimulationResult();
		
		ScheduleStop previousStop = null;
		ScheduleStop currentStop = null;
		int directionIdx = 0;
		Calendar cal = Calendar.getInstance();
		
		while(canRun) {
			BusTrip trip = direction.poll();
			
			if(trip == null) {
				canRun = false;
				break;
			} 
			
			List<ScheduleStop> tripStops = trip.getStops();
			
			for(int i=0; i<tripStops.size(); i++) {
				System.out.println("stop "+i);
				if(i != 0) {
					previousStop = currentStop;
				} else {
					previousStop = null;
				}

				currentStop = tripStops.get(i);
				String arrivalTime = currentStop.getArrival();
				double batteryState;
				
				if( currentStop != null && previousStop != null ) {
					int meters = this.getDistance(previousStop, currentStop);
					double consumption = this.consumptionProfile.getConsumption(bus.getType(), null);
					
					try {
						this.bus.drive(meters, consumption);
					} catch(IllegalStateException e) {
						result.addBatteryStateEntry(new BatteryStateEntry(this.bus.getBatteryState(), arrivalTime, currentStop.getStopId()));
						result.setSurvived(false);
						return result;
					} finally {
						result.addBatteryStateEntry(new BatteryStateEntry(this.bus.getBatteryState(), arrivalTime, currentStop.getStopId()));
					}
				}
				
				if(i != 0) {
					ElectrifiedBusStop elStop = this.getElectrified(currentStop.getStopId());
					
					if(elStop != null) {
						int chargingTimeSeconds = this.getChargingTime(elStop, trip.getRouteId());
						BusCharger chargerType = elStop.getCharger(currentStop.getArrival(), chargingTimeSeconds).getType();
						int timeSpentCharging = this.bus.charge(chargingTimeSeconds, chargerType.getPower());
						batteryState = this.bus.getBatteryState();
						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrivalTime.substring(0, 2), 10));
						cal.set(Calendar.MINUTE, Integer.parseInt(arrivalTime.substring(2, 4), 10));
						cal.set(Calendar.SECOND, 0);
						cal.add(Calendar.SECOND, timeSpentCharging);
						
						result.addBatteryStateEntry(new BatteryStateEntry(batteryState, (new SimpleDateFormat("HHmm")).format(cal.getTime()), currentStop.getStopId()));
					}
				} else {
					result.addBatteryStateEntry(new BatteryStateEntry(this.bus.getBatteryState(), currentStop.getArrival(), currentStop.getStopId()));
				}
				
			}
			
			directionIdx = (directionIdx + 1) % 2;
			
			if(directionIdx == 0) {
				direction = this.getDirectionA();
			} else {
				direction = this.getDirectionB();
			}
		}
		
		result.setSurvived(true);
		
		return result;
	}
	
	public int getChargingTime(ElectrifiedBusStop elStop, String routeId) {
		if(elStop.getChargingTimes().get(routeId) == null) {
			return 10;
		} else {
			return elStop.getChargingTimes().get(routeId);
		}
	}

	/**
	 * Returns distance between two consecutive bus stops in meters
	 * @param previousStop
	 * @param currentStop
	 * @return
	 */
	private int getDistance(ScheduleStop previousStop, ScheduleStop currentStop) {
		return 1000;
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
