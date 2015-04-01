package configuration.emn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.BusRoute;
import model.BusStop;
import model.BusTrip;
import model.ScheduleStop;

import org.mongodb.morphia.Datastore;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;

import dto.jobstate.ScheduleImportJobState;
import play.Logger;
import utils.MongoUtils;

public class RuterGTFSHandler {

	private GtfsReader reader;
	private GtfsDaoImpl store;
	private static int BUS_ROUTE_TYPE = 3;
	
	public RuterGTFSHandler(String gtfsDir) throws IOException {
		this.reader = new GtfsReader();
		this.reader.setInputLocation(new File(gtfsDir));
		this.store = new GtfsDaoImpl();
		this.reader.setEntityStore(this.store);
	}

	public void parseInput() throws IOException {
		this.reader.run();
	}

	public GtfsDaoImpl getStore() {
		return this.store;
	}
	
	public void dumpData() {
		Datastore ds = MongoUtils.ds();
		ds.delete(ds.createQuery(BusRoute.class));
		//ds.delete(ds.createQuery(BusStop.class));
		ds.delete(ds.createQuery(BusTrip.class));


		Map<String, List<ServiceCalendarDate>> opDates = this.processDates();
		Map<String, BusRoute> routes = this.processRoutes();
		Map<String, BusTrip> trips = new HashMap<String, BusTrip>();
		Map<String, BusStop> stops = new HashMap<String, BusStop>();
		
		ds.save(routes.values());
		
		Logger.info(routes.size() + " routes imported");

		for (StopTime st : store.getAllStopTimes()) {
			
			String stopId = st.getStop().getId().getId();
			Trip t = st.getTrip();
			Route r = t.getRoute();
			
			if(r.getType() != BUS_ROUTE_TYPE) {
				continue;
			}
			
			BusStop busStop = stops.get(stopId);
			BusTrip busTrip = trips.get(st.getTrip().getId().getId());
			
			if (busTrip == null) {
				busTrip = this.createTrip(t, opDates);
				trips.put(st.getTrip().getId().getId(), busTrip);
			}

			if (busStop == null){
				Stop stop = st.getStop();
				busStop = new BusStop(stop);
				stops.put(stopId, busStop);
			}

			ScheduleStop scheduleStop = new ScheduleStop(st, busStop);
			busTrip.addStop(scheduleStop);
		}
		
		ds.save(trips.values());


//		ds.save(trips.values());
//		Logger.info(trips.size() + " trips imported");
	}

	private BusTrip createTrip(Trip t, Map<String, List<ServiceCalendarDate>> opDates) {
		BusTrip trip = new BusTrip();
		trip.setDataSource("ruter");
		trip.setDates(opDates.get(t.getServiceId().getId()));
		String tripId = t.getId().getId();

		trip.setServiceID(tripId);
		trip.setFootnoteId(t.getServiceId().getId()); // references service
														// id in calendar
														// dates
		trip.setRoute(t.getRoute().getShortName());
		trip.setRouteId(t.getRoute().getId().getId());
		trip.setDirection(t.getDirectionId());
		return trip;
	}


	private Map<String, List<ServiceCalendarDate>> processDates() {
		HashMap<String, List<ServiceCalendarDate>> opDates = new HashMap<String, List<ServiceCalendarDate>>();

		for (ServiceCalendarDate scd : this.store.getAllCalendarDates()) {
			// Service is not operating on that day
			if (scd.getExceptionType() == 2) {
				continue;
			}

			String serviceId = scd.getServiceId().getId();

			if (opDates.containsKey(serviceId)) {
				opDates.get(serviceId).add(scd);
			} else {
				List<ServiceCalendarDate> list = new ArrayList<ServiceCalendarDate>();
				list.add(scd);
				opDates.put(serviceId, list);
			}
		}
		return opDates;
	}

	private HashMap<String, BusRoute> processRoutes() {
		HashMap<String, BusRoute> busRoutes = new HashMap<String, BusRoute>();
		
		for (Route r : this.store.getAllRoutes()) {
			if (r.getType() == BUS_ROUTE_TYPE) {
				BusRoute route = new BusRoute();
				route.setRouteId(r.getId().getId());
				route.setDescription(r.getDesc());
				route.setName(r.getShortName());
				busRoutes.put(route.getRouteId(), route);
			}
		}

		return busRoutes;
	}
}
