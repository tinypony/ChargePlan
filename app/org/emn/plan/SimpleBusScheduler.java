package org.emn.plan;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import utils.DateUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import model.dataset.BusTrip;
import model.dataset.ScheduleStop;
import model.planning.ElectrifiedBusStop;

public class SimpleBusScheduler {
	
	Queue<BusTrip> dirA;
	Queue<BusTrip> dirB;
	int minWaitingTime;
	public static final int DEFAULT__END_STOP_WAITING_TIME = 600;
	
	public SimpleBusScheduler() {
		this(DEFAULT__END_STOP_WAITING_TIME);
	}
	
	public SimpleBusScheduler(int minWaitingTime) {
		this.minWaitingTime = minWaitingTime;
	}

	public void schedule(List<BusTrip> trips, List<ElectrifiedBusStop> eStops) {
		int first = 0;
		dirA = new LinkedBlockingQueue<BusTrip>();
		dirB = new LinkedBlockingQueue<BusTrip>();
		
		List<BusTrip> trips0 =  Lists.newArrayList(Iterables.filter(trips, new Predicate<BusTrip>() {
			@Override
			public boolean apply(BusTrip arg0) {
				return "0".equals(arg0.getDirection());
			}
		}));
		
		List<BusTrip> trips1 =  Lists.newArrayList(Iterables.filter(trips, new Predicate<BusTrip>() {
			@Override
			public boolean apply(BusTrip arg0) {
				return "1".equals(arg0.getDirection());
			}
		}));
		
		Collections.sort(trips0);
		Collections.sort(trips1);
		
		if(trips0.get(0).compareTo(trips1.get(0)) < 0) {
			this.process(trips0, trips1, eStops);
		} else {
			this.process(trips1, trips0, eStops);
		}
	}
	
	private void process(List<BusTrip> tripsA, List<BusTrip> tripsB, List<ElectrifiedBusStop> eStops) {
		//pointer0, pointer1
		int pA = 0, pB = 0;
		//time0, time1
		Calendar calA = Calendar.getInstance();
		Calendar calB = Calendar.getInstance();
		
		calA.set(Calendar.HOUR_OF_DAY, 0);
		calA.set(Calendar.MINUTE, 0);
		calA.set(Calendar.SECOND, 0);
		
		calB.set(Calendar.HOUR_OF_DAY, 0);
		calB.set(Calendar.MINUTE, 0);
		calB.set(Calendar.SECOND, 0);
		
		boolean hasConsecutiveTrips = true;
		Calendar candidateTime = Calendar.getInstance();
		
		whileloop:
		while(hasConsecutiveTrips) {
			if(pA >= tripsA.size() || pB >= tripsB.size()) {
				hasConsecutiveTrips = false;
				break whileloop;
			}
			
			for(; pA < tripsA.size(); pA++) {
				BusTrip candidateA = tripsA.get(pA);
				
				List<ScheduleStop> stopsA = candidateA.getStops();
				ScheduleStop firstStop = stopsA.get(0);
				candidateTime = DateUtils.stringToCalendar(firstStop.getArrival());
				
				if(candidateTime.compareTo(calA) >= 0) {
					dirA.add(candidateA);
					ScheduleStop lastStop = stopsA.get(stopsA.size() - 1);
					Calendar tripLastStopTime = DateUtils.stringToCalendar(lastStop.getArrival());
					int chargingTime = this.getChargingTime(candidateA.getRouteId(), lastStop, eStops);
					tripLastStopTime.add(Calendar.SECOND, chargingTime);

					calB.set(Calendar.HOUR_OF_DAY, tripLastStopTime.get(Calendar.HOUR_OF_DAY));
					calB.set(Calendar.MINUTE, tripLastStopTime.get(Calendar.MINUTE));
					calB.set(Calendar.SECOND, 0);
					pA++;
					
					if(this.isOvernight(candidateA)) {
						break whileloop;
					}
					
					break;
				} 
				
				if (pA >= tripsA.size() - 1) {
					hasConsecutiveTrips = false;
					break whileloop;
				}
			}
			
			for(; pB<tripsB.size(); pB++) {
				BusTrip candidateB = tripsB.get(pB);
				List<ScheduleStop> stopsB = candidateB.getStops();
				ScheduleStop firstStop = stopsB.get(0);
				candidateTime = DateUtils.stringToCalendar(firstStop.getArrival());
				
				if(candidateTime.compareTo(calB) >= 0) {
					dirB.add(candidateB);ScheduleStop lastStop = stopsB.get(stopsB.size() - 1);
					Calendar tripLastStopTime = DateUtils.stringToCalendar(lastStop.getArrival());
					
					int chargingTime = this.getChargingTime(candidateB.getRouteId(), lastStop, eStops);
					tripLastStopTime.add(Calendar.SECOND, chargingTime);
					
					calA.set(Calendar.HOUR_OF_DAY, tripLastStopTime.get(Calendar.HOUR_OF_DAY));
					calA.set(Calendar.MINUTE, tripLastStopTime.get(Calendar.MINUTE));
					calA.set(Calendar.SECOND, 0);
					pB++;
					
					if(this.isOvernight(candidateB)) {
						break whileloop;
					}
					
					break;
				}
				
				if (pB >= tripsA.size() - 1) {
					hasConsecutiveTrips = false;
					break whileloop;
				}
			}
		}
	}

	private boolean isOvernight(BusTrip candidateA) {
		ScheduleStop stopOne = candidateA.getStops().get(0);
		ScheduleStop lastStop = candidateA.getStops().get(candidateA.getStops().size() - 1);
		Calendar calFirst = DateUtils.stringToCalendar(stopOne.getArrival());
		Calendar calLast = DateUtils.stringToCalendar(lastStop.getArrival());
		
		return calFirst.compareTo(calLast) > 0;
	}

	private int getChargingTime(String routeId, final ScheduleStop lastStop,
			List<ElectrifiedBusStop> eStops) {
		ElectrifiedBusStop electrifiedStop = Iterables.find(eStops, new Predicate<ElectrifiedBusStop>() {

			@Override
			public boolean apply(ElectrifiedBusStop arg0) {
				return arg0.getStopId().equals(lastStop.getStopId()); 
			}
			
		}, null);
		
		if(electrifiedStop == null || electrifiedStop.getChargingTime(routeId) == null) {
			return this.minWaitingTime;
		} else {
			return electrifiedStop.getChargingTime(routeId);
		}
	}

	public Queue<BusTrip> getDirectionA() {
		return this.dirA;
	}

	public Queue<BusTrip> getDirectionB() {
		return this.dirB;
	}
}
