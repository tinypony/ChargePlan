package model.dataset;

import org.mongodb.morphia.annotations.*;

@Entity("distances")
public class StopDistance {

	private String from;
	private String to;
	private int distance;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
}
