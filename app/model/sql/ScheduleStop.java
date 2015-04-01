package model.sql;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.onebusaway.gtfs.model.StopTime;
import org.xml.sax.Attributes;


@Entity
public class ScheduleStop implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private int stopOrder;
	private String arrival;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private BusStop stop;

	//Creating from hsl database
	public ScheduleStop(Attributes atts, Map<String, BusStop> stops)
			throws IllegalArgumentException {
		this.stop = stops.get(atts.getValue("StationId"));

		if (this.stop == null) {
			throw new IllegalArgumentException("Cannot find a specified stop");
		}

		this.stopOrder = Integer.parseInt(atts.getValue("Ix"));
		this.arrival = atts.getValue("Arrival");

		if (this.arrival == null) {
			throw new IllegalArgumentException("Arrival was not specified");
		}
	}

	public ScheduleStop(StopTime st, BusStop bstop) {
		this.stop = bstop;
		this.stopOrder = st.getStopSequence();
		this.arrival = this.getTime("HHmm", st.getArrivalTime());
	}

	public ScheduleStop() {

	}
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BusStop getStop() {
		return stop;
	}

	public void setStop(BusStop stop) {
		this.stop = stop;
	}

	public int getOrder() {
		return stopOrder;
	}

	public void setOrder(int order) {
		this.stopOrder = order;
	}

	public String getArrival() {
		return arrival;
	}

	public void setArrival(String arrival) {
		this.arrival = arrival;
	}

	public String getTime(String format, int secondsFromMidnight) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

		return df.format(secondsFromMidnight * 1000);
	}


	public int compareTo(ScheduleStop arg0) {
		ScheduleStop another = (ScheduleStop) arg0;
		return this.stopOrder - another.getOrder();
	}
}
