package model.dataset;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

@Entity
@Embedded
public class Waypoint {

	int pointOrder;
	String stopId;
	
	public Waypoint() {
		
	}
	
	public Waypoint(int order, String stopId) {
		this.pointOrder = order;
		this.stopId = stopId;
	}

	public int getPointOrder() {
		return pointOrder;
	}

	public void setPointOrder(int pointOrder) {
		this.pointOrder = pointOrder;
	}

	public String getStopId() {
		return stopId;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
}
