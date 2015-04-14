package model.dataset;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class RouteDirection {
	List<BusStop> stops;
	
	public RouteDirection(){
		
	}
	
	public void setStops(List<ScheduleStop> sstops) {
		this.stops = Lists.transform(sstops, new Function<ScheduleStop, BusStop>(){
			public BusStop apply(ScheduleStop s) {
				return s.getStop();
			}
		});
	}
	
	public List<BusStop> getStops() {
		return this.stops;
	}
}
