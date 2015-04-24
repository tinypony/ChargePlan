package org.emn.plan;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.DatatypeConverter;

import org.emn.calculate.IConsumptionProfile;

import scala.util.control.Exception.Finally;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import configuration.emn.route.StopsDistanceRetriever;
import model.dataset.BusTrip;
import model.dataset.ScheduleStop;
import model.dataset.StopDistance;
import model.planning.BusInstance;
import model.planning.ElectrifiedBusStop;
import model.planning.solutions.BusCharger;
import model.planning.solutions.ElectricBus;

public class RouteSimulationModel {

	private Queue<BusTrip> directionA;
	private Queue<BusTrip> directionB;
	private List<ElectrifiedBusStop> electrifiedStops;
	private BusInstance bus;
	Calendar simDate;
	private StopsDistanceRetriever distanceManager;
	private IConsumptionProfile consumptionProfile;
	
	public static final int DEFAULT_END_STOP_CHARGING_TIME = 600;
	public static final int DEFAULT_STOP_CHARGING_TIME = 10;
	
	public RouteSimulationModel(IConsumptionProfile profile, BusInstance bus, String simDate) throws ParseException {
		this.consumptionProfile = profile;
		this.bus = bus;
		this.simDate = DatatypeConverter.parseDate(simDate);
		
		this.simDate.set(Calendar.HOUR_OF_DAY, 0);
		this.simDate.set(Calendar.MINUTE, 0);
		this.simDate.set(Calendar.SECOND, 0);
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

	public SimulationResult simulate() throws Exception {
		boolean canRun = true;
		Queue<BusTrip> direction = this.getDirectionA();
		SimulationResult result = new SimulationResult();
		ScheduleStop previousStop = null;
		ScheduleStop currentStop = null;
		int directionIdx = 0;
		
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
				} else {
					previousStop = null;
				}

				currentStop = tripStops.get(i);
				String arrivalTime = currentStop.getArrival();
				simDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrivalTime.substring(0, 2), 10));
				simDate.set(Calendar.MINUTE, Integer.parseInt(arrivalTime.substring(2, 4), 10));
				simDate.set(Calendar.SECOND, 0);
				
				if( currentStop != null && previousStop != null ) {
					int meters = this.getDistanceManager().getDistanceBetweenStops(previousStop, currentStop);
					double consumption = this.consumptionProfile.getConsumption(bus.getType(), null);
					
					try {
						this.bus.drive(meters, consumption);
					} catch(IllegalStateException e) {
						this.addBatteryEntry( result, this.bus.getPercentageBatteryState(), this.simDate, currentStop.getStopId() );
						result.setSurvived(false);
						return result;
					} finally {
						this.addBatteryEntry(result, this.bus.getPercentageBatteryState(), this.simDate, currentStop.getStopId());
					}
				}
				
				if(i != 0) {
					ElectrifiedBusStop elStop = this.getElectrified(currentStop.getStopId());
					
					if(elStop != null && elStop.getCharger() !=null) {
						int chargingTimeSeconds = this.getChargingTime(elStop, trip.getRouteId(), i == tripStops.size() - 1);
						BusCharger chargerType = elStop.getCharger(currentStop.getArrival(), chargingTimeSeconds).getType();
						int timeSpentCharging = this.bus.charge(chargingTimeSeconds, chargerType.getPower());
						
						this.simDate.add(Calendar.SECOND, timeSpentCharging);
						
						this.addBatteryEntry(result, this.bus.getPercentageBatteryState(), this.simDate, currentStop.getStopId());
					}
				} else {
					this.addBatteryEntry(result, this.bus.getPercentageBatteryState(), this.simDate, currentStop.getStopId());
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
	
	private void addBatteryEntry(SimulationResult result, double soc, Calendar cal, String location) {
		BatteryStateEntry lastEntry = result.getLastBatteryStateEntry();
		if(lastEntry !=null && lastEntry.getTimestamp().compareTo(cal.getTime()) > 0) {
			//increment day
			cal.add(Calendar.DATE, 1);
		}
		result.addBatteryStateEntry(new BatteryStateEntry(soc, cal.getTime(), location));
	}
	
	public int getChargingTime(ElectrifiedBusStop elStop, String routeId, boolean isEndStop) {
		if(elStop.getChargingTimes().get(routeId) == null) {
			if(isEndStop)	return DEFAULT_END_STOP_CHARGING_TIME;
			else			return DEFAULT_STOP_CHARGING_TIME;
		} else {
			return elStop.getChargingTimes().get(routeId);
		}
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

	public StopsDistanceRetriever getDistanceManager() {
		return distanceManager;
	}

	public void setDistanceManager(StopsDistanceRetriever distanceManager) {
		this.distanceManager = distanceManager;
	}
}
