package configuration.emn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import model.BusRoute;
import model.BusStop;
import model.BusTrip;
import model.ScheduleStop;
import model.Waypoint;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
		ds.delete(ds.createQuery(BusStop.class));
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

			if (!processRoute(r)) {
				continue;
			}

			BusStop busStop = stops.get(stopId);
			BusTrip busTrip = trips.get(st.getTrip().getId().getId());

			if (busTrip == null) {
				busTrip = this.createTrip(t, opDates);
				trips.put(st.getTrip().getId().getId(), busTrip);
			}

			if (busStop == null) {
				Stop stop = st.getStop();
				busStop = new BusStop(stop);
				stops.put(stopId, busStop);
			}

			ScheduleStop scheduleStop = new ScheduleStop(st, busStop);
			busTrip.addStop(scheduleStop);
		}

		for (BusTrip trip : trips.values()) {
			final List<ScheduleStop> list = trip.getStops();
			Collections.sort(list);
			trip.setStops(list);
			
			try {
				ScheduleStop first = Iterables.find(list,
						new Predicate<ScheduleStop>() {
							@Override
							public boolean apply(ScheduleStop arg0) {
								return arg0.getOrder() == 1;
							}
						}, list.get(0));

				first.getStop().setFirst(true);

			} catch (NoSuchElementException e) {
				Logger.warn("Cannot find first stop for " + trip.getServiceID());
			}

			try {
				ScheduleStop last = Iterables.find(list,
						new Predicate<ScheduleStop>() {
							@Override
							public boolean apply(ScheduleStop arg0) {
								return arg0.getOrder() == list.size();
							}
						}, list.get(list.size()-1));

				last.getStop().setLast(true);
			} catch (NoSuchElementException e) {
				Logger.warn("Cannot find last stop for " + trip.getServiceID());

			}

		}

		ds.save(trips.values());
		ds.save(stops.values());

		// ds.save(trips.values());
		// Logger.info(trips.size() + " trips imported");
	}

	// public void augmentRoutes() {
	// Datastore ds = MongoUtils.ds();
	//
	// Query<BusRoute> q = ds.createQuery(BusRoute.class);
	// q.field("name").equal(Pattern.compile("^N{0,1}\\d\\d[A-Za-z]{0,1}$"));
	// List<BusRoute> routes = q.asList();
	//
	// for(BusRoute r: routes) {
	// Query<BusTrip> qr = ds.createQuery(BusTrip.class);
	// BusTrip trip = qr.field("routeId").equal(r.getRouteId()).get();
	// List<ScheduleStop> stops = trip.getStops();
	// Collections.sort(stops);
	//
	// List<Waypoint> waypoints = Lists.transform(stops, new
	// Function<ScheduleStop, Waypoint>(){
	// public Waypoint apply(ScheduleStop s) {
	// return new Waypoint(s.getOrder(), s.getStop().getStopId());
	// }
	// });
	// System.out.println(waypoints.size());
	// r.setWaypoints(waypoints);
	// }
	// ds.save(routes);
	// }

	private BusTrip createTrip(Trip t,
			Map<String, List<ServiceCalendarDate>> opDates) {
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
			if (processRoute(r)) {
				BusRoute route = new BusRoute();
				route.setRouteId(r.getId().getId());
				route.setDescription(r.getDesc());
				route.setName(r.getShortName());
				busRoutes.put(route.getRouteId(), route);
			}
		}

		return busRoutes;
	}

	private boolean processRoute(Route r) {
		return r.getType() == BUS_ROUTE_TYPE
				&& r.getShortName().matches("^N{0,1}\\d\\d[A-Za-z]{0,1}$");
	}
}
