package model.dataset;

import java.util.ArrayList;
import java.util.Calendar;

import org.mongodb.morphia.annotations.Embedded;


public class BusTripGroup {
	private String serviceId;
	private String routeId;
	private int numOfStops;
	private String direction;
	
	@Embedded
	private ArrayList<ScheduleStop> stops; // stop
	

	public BusTripGroup() {
		this.stops = new ArrayList<ScheduleStop>();
	}

	public void addStop(ScheduleStop stop) {
		this.stops.add(stop);
	}

	public ArrayList<ScheduleStop> getStops() {
		return this.stops;
	}

	public String getServiceID() {
		return serviceId;
	}

	public void setServiceID(String serviceID) {
		this.serviceId = serviceID;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}


	public String getDateString(Calendar cal) {
		return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1)
				+ "-" + cal.get(Calendar.DATE);
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public int getNumOfStops() {
		return numOfStops;
	}

	public void setNumOfStops(int numOfStops) {
		this.numOfStops = numOfStops;
	}	
}
