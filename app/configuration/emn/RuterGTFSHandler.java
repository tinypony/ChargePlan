package configuration.emn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import model.sql.BusRoute;
import model.sql.BusStop;
import model.sql.BusTrip;
import model.sql.ScheduleStop;

import org.hibernate.SessionFactory;
import org.mongodb.morphia.Datastore;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
import play.Logger;
import utils.JPAUtils;
public class RuterGTFSHandler {

	private GtfsReader reader;
	private GtfsDaoImpl store;
	private static int BUS_ROUTE_TYPE = 3;
	private String gtfsDir;
	
	public RuterGTFSHandler(String gtfsDir) throws IOException {
		this.gtfsDir = gtfsDir;
		this.reader = new GtfsReader();
		this.reader.setInputLocation(new File(this.gtfsDir));
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
		Logger.info("Convert to entities");
		Logger.info("Start import");
		EntityManager em = JPAUtils.em();
		em.getTransaction().begin();
		
		if(em.createQuery("from BusRoute").getResultList().size() > 0 ) {
			em.createQuery("delete BusRoute").executeUpdate();
		}
		
		if(em.createQuery("from BusTrip").getResultList().size() > 0 ) {
			em.createQuery("delete BusTrip").executeUpdate();
		}
		
		if(em.createQuery("from ScheduleStop").getResultList().size() > 0 ) {
			em.createQuery("delete ScheduleStop").executeUpdate();
		}
		
		if(em.createQuery("from BusStop").getResultList().size() > 0 ) {
			em.createQuery("delete BusStop").executeUpdate();
		}
		
	//	Map<String, BusRoute> routes = this.processRoutes();
		Map<String, List<ServiceCalendarDate>> opDates = this.processDates();
		
//		Map<String, BusTrip> trips = this.processTrips(routes, opDates);
		Map<String, BusStop> stops = this.processStops(opDates);
		//this.postprocessTrips(trips);

//
////		Logger.info(stops.size() + " stops imported");
////		
////
////		Logger.info(routes.size() + " routes imported");
////
////
////		Logger.info(trips.size() + " trips imported");
//		int i = 0;
//		
//		for(BusTrip bt: trips.values()) {
//			em.persist(bt);
//			i++;
//			Logger.info("Persisted "+i+" trips");
//			if (i % 300 == 0) {
//				em.flush();
//				Logger.info("Flush");
//			}
//		}
//		
//		for(BusRoute r: routes.values()) {
//			em.persist(r);
//			i++;
//			Logger.info("Persisted "+i+" routes");
//			if (i % 10 == 0) {
//				em.flush();
//				Logger.info("Flush");
//			}
//		}
//		
//		Logger.info(routes.size() + " routes imported");
//		Logger.info(trips.size() + " trips imported");
//		Logger.info(stops.size() + " stops imported");
//		
		em.getTransaction().commit();
	}


	private Map<String, BusStop> processStops(Map<String, List<ServiceCalendarDate>> opDates) {
		HashMap<String, BusTrip> trips = new HashMap<String, BusTrip>();
		HashMap<String, BusStop> stops = new HashMap<String, BusStop>();
		HashMap<String, BusRoute> routes = new HashMap<String, BusRoute>();
		long i = 0L;
		
		for (StopTime st : store.getAllStopTimes()) {
			//onebusaway objects
			Route r = st.getTrip().getRoute();
			Trip t = st.getTrip();
			
			//don't handle non-bus stops
			if(r.getType() != BUS_ROUTE_TYPE) {
				continue;
			}
			
			String stopId = st.getStop().getId().getId();
			BusStop busStop = stops.get(stopId);
			BusTrip busTrip = trips.get(t.getId().getId());
			BusRoute busRoute = routes.get(r.getId().getId());
			ScheduleStop scheduleStop;
			
			if(busStop == null) {
				busStop = this.createBusStop(st.getStop());
				stops.put(stopId, busStop);
				JPAUtils.em().persist(busStop);
			}
			
			scheduleStop = this.createScheduleStop(st, busStop);
			JPAUtils.em().persist(scheduleStop);
			
			if(busTrip == null) {
				busTrip = this.createBusTrip(st, opDates.get(t.getServiceId().getId()));
				busTrip.addStop(scheduleStop);
				trips.put(t.getId().getId(), busTrip);
				JPAUtils.em().persist(busTrip);
			} else {
				busTrip.addStop(scheduleStop);
			}
		
			if(busRoute == null) {
				busRoute = this.createBusRoute(r);
				routes.put(r.getId().getId(), busRoute);
				JPAUtils.em().persist(busRoute);
			} else if(!busRoute.getTrips().contains(busTrip)) {
				busRoute.getTrips().add(busTrip);
			}
			
			i++;
			if(i % 1000 == 0) {
				JPAUtils.em().flush();
			}
			Logger.info("Imported "+i+" stop times");
			
		}
		return stops;
	}
	
	private BusRoute createBusRoute(Route r) {
		BusRoute route = new BusRoute();
		route.setRouteId(r.getId().getId());
		route.setDescription(r.getDesc());
		route.setName(r.getShortName());
		return route;
	}


	private ScheduleStop createScheduleStop(StopTime st, BusStop busStop) {
		return new ScheduleStop(st, busStop);
		
	}


	private BusStop createBusStop(Stop stop) {
		return new BusStop(stop);
	}


	private BusTrip createBusTrip(StopTime st, List<ServiceCalendarDate> dates) {
		Route r = st.getTrip().getRoute();
		Trip t = st.getTrip();
		
		BusTrip trip = new BusTrip();
		trip.setDates(dates);
		trip.setDataSource("ruter");
		String tripId = t.getId().getId();

		trip.setServiceID(tripId);
		trip.setFootnoteId(t.getServiceId().getId()); // references service
														// id in calendar
														// dates
		trip.setRoute(r.getShortName());
		trip.setServiceNbr(t.getRoute().getId().getId());
		trip.setDirection(t.getDirectionId());
		return trip;
	}

	private Map<String, BusTrip> processTrips(Map<String, BusRoute> busRoutes,
			Map<String, List<ServiceCalendarDate>> opDates) {
		HashMap<String, BusTrip> trips = new HashMap<String, BusTrip>();

		for (Trip t : store.getAllTrips()) {
			BusRoute route = busRoutes.get(t.getRoute().getId().getId());
			
			if(route == null || t.getRoute().getType() != BUS_ROUTE_TYPE) {
				continue;
			}
			
			BusTrip trip = new BusTrip();
			trip.setDates(opDates.get(t.getServiceId().getId()));
			trip.setDataSource("ruter");
			String tripId = t.getId().getId();

			trip.setServiceID(tripId);
			trip.setFootnoteId(t.getServiceId().getId()); // references service
															// id in calendar
															// dates
			trip.setRoute(t.getRoute().getShortName());
			trip.setServiceNbr(t.getRoute().getId().getId());
			trip.setDirection(t.getDirectionId());
			
			//set relation
			trip.setRouteRef(route);
			route.getTrips().add(trip);
			
			trips.put(tripId, trip);
		}

		return trips;
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
				JPAUtils.em().persist(route);
				JPAUtils.em().detach(route);
				busRoutes.put(route.getRouteId(), route);
			}
		}

		return busRoutes;
	}
}
