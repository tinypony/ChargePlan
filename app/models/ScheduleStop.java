package models;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.json.JSONObject;
import org.mongodb.morphia.annotations.Embedded;
import org.onebusaway.gtfs.model.StopTime;
import org.xml.sax.Attributes;

import com.mongodb.BasicDBObject;

@Embedded
public class ScheduleStop implements Jsonable, Mongoable, Comparable {
	private BusStop stop;
	private int order;
	private String arrival;

	@Override
	public JSONObject toJson() {
		JSONObject jstop = new JSONObject();
		jstop.put("id", this.getStop().getStopId());
		jstop.put("time", this.getArrival());
		jstop.put("name", this.getStop().getName());
		jstop.put("posX", this.getStop().getX());
		jstop.put("posY", this.getStop().getY());
		return jstop;
	}

	public ScheduleStop(Attributes atts, Map<String, BusStop> stops)
			throws IllegalArgumentException {
		this.stop = stops.get(atts.getValue("StationId"));

		if (this.stop == null) {
			throw new IllegalArgumentException("Cannot find a specified stop");
		}

		this.order = Integer.parseInt(atts.getValue("Ix"));
		this.arrival = atts.getValue("Arrival");

		if (this.arrival == null) {
			throw new IllegalArgumentException("Arrival was not specified");
		}
	}

	public ScheduleStop(StopTime st, BusStop bstop) {
		this.stop = bstop;
		this.order = st.getStopSequence();
		this.arrival = this.getTime("HHmm", st.getArrivalTime());
	}

	public ScheduleStop() {

	}

	public BusStop getStop() {
		return stop;
	}

	public void setStop(BusStop stop) {
		this.stop = stop;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getArrival() {
		return arrival;
	}

	public void setArrival(String arrival) {
		this.arrival = arrival;
	}

	@Override
	public BasicDBObject toMongoObj() {
		BasicDBObject jstop = new BasicDBObject();
		jstop.append("id", this.getStop().getStopId()).append("order", this.order)
				.append("time", this.getArrival())
				.append("name", this.getStop().getName())
				.append("posX", this.getStop().getX())
				.append("posY", this.getStop().getY());
		return jstop;
	}

	public String getTime(String format, int secondsFromMidnight) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

		return df.format(secondsFromMidnight * 1000);
	}

	@Override
	public int compareTo(Object arg0) {
		ScheduleStop another = (ScheduleStop) arg0;
		return this.order - another.getOrder();
	}
}
