package org.emn.plan;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import model.dataset.BusTrip;

public class SimpleBusScheduler {
	
	Queue<BusTrip> dirA;
	Queue<BusTrip> dirB;

	public void schedule(List<BusTrip> trips, int minWaitingTime) {
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
			this.process(trips0, trips1, minWaitingTime);
		} else {
			this.process(trips1, trips0, minWaitingTime);
		}
	}
	
	private void process(List<BusTrip> tripsA, List<BusTrip> tripsB, int minWaitingTime) {
		//pointer0, pointer1
		//time0, time1
		//while
			//for
			//for
	}

	public Queue<BusTrip> getDirectionA() {
		// TODO Auto-generated method stub
		return this.dirA;
	}

	public Queue<BusTrip> getDirectionB() {
		// TODO Auto-generated method stub
		return this.dirB;
	}
}
