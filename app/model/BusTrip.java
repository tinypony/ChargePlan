package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import org.mongodb.morphia.annotations.*;

@Entity("trips")
public class BusTrip /*implements Jsonable, Mongoable*/ {
	@Id
	ObjectId id;
	private String serviceId;
	private String routeId;
	private String route;
	@Transient
	private BusRoute routeRef;
	private String companyId;
	private String footnoteId;
	private String direction;
	private List<String> dates;
	private int tripLength;
	
	@Embedded
	private ArrayList<ScheduleStop> stops; // stop
	
	@Transient
	private String firstDate;
	@Transient
	private String vector;
	@Transient 
	private String trnsMode;
	@Transient
	private String dataSource;

	public BusTrip() {
		this.stops = new ArrayList<ScheduleStop>();
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getRoute() {
		return this.route;
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

	public void setCompany(String companyId) {
		this.companyId = companyId;
	}

	public String getCompany() {
		return this.companyId;
	}


	public List<String> getDates() {
//		BasicDBList dates = new BasicDBList();
//		Calendar cal = Calendar.getInstance();
//
//		if (dataSource == "hsl") {
//			String[] tokens = firstDate.split("-");
//
//			cal.set(Integer.parseInt(tokens[0]),
//					Integer.parseInt(tokens[1]) - 1,
//					Integer.parseInt(tokens[2]) - 1);
//
//			for (int i = 0; i < this.getVector().length(); i++) {
//				char a = this.getVector().charAt(i);
//				if (a == '1') {
//					cal.set(Integer.parseInt(tokens[0]),
//							Integer.parseInt(tokens[1]) - 1,
//							Integer.parseInt(tokens[2]));
//					cal.add(Calendar.DATE, i);
//					dates.add(this.getDateString(cal));
//				}
//			}
//		} else {
//			dates.addAll(this.dates);
//		}

		return this.dates;
	}

	public void setDates(List<ServiceCalendarDate> calDates) {
		ArrayList<String> dates = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		
		for (ServiceCalendarDate scd : calDates) {
			cal.set(scd.getDate().getYear(), scd.getDate().getMonth() - 1, scd
					.getDate().getDay());
			dates.add(this.getDateString(cal));
		}

		this.dates = dates;
	}

	public String getDateString(Calendar cal) {
		return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1)
				+ "-" + cal.get(Calendar.DATE);
	}

	public String getFootnoteId() {
		return footnoteId;
	}

	public void setFootnoteId(String footnoteId) {
		this.footnoteId = footnoteId;
	}

	public String getFirstDate() {
		return firstDate;
	}

	public void setFirstDate(String firstDate) {
		this.firstDate = firstDate;
	}

	public String getVector() {
		return vector;
	}

	public void setVector(String vector) {
		this.vector = vector;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getTrnsmode() {
		return trnsMode;
	}

	public void setTrnsmode(String trnsMode) {
		this.trnsMode = trnsMode;
	}

	public BusRoute getRouteRef() {
		return routeRef;
	}

	public void setRouteRef(BusRoute routeRef) {
		this.routeRef = routeRef;
	}

	public int getTripLength() {
		return tripLength;
	}

	public void setTripLength(int tripLength) {
		this.tripLength = tripLength;
	}
	
	
}
