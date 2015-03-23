package configuration.emn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import models.BusStop;
import models.BusTrip;
import models.ScheduleStop;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;


public class RuterGTFSHandler {

    private GtfsReader reader;
    private GtfsDaoImpl store;
    private static int  BUS_ROUTE_TYPE = 3;
    
    HashMap<String, Route> routes = new HashMap<String, Route>();
    HashMap<String, List<ServiceCalendarDate>> opDates = new HashMap<String, List<ServiceCalendarDate>>();
    HashMap<String, BusTrip> trips = new HashMap<String, BusTrip>();
    HashMap<String, BusStop> stops = new HashMap<String, BusStop>();
    
    
    public RuterGTFSHandler(String gtfsDir) throws IOException {
        this.reader = new GtfsReader();
        this.reader.setInputLocation(new File(gtfsDir));
        this.store = new GtfsDaoImpl();
        this.reader.setEntityStore(this.store);
    }
    
    public void parseInput() throws IOException {
        this.reader.run();
        System.out.println("GTFS files fully read");
    }
    
    public GtfsDaoImpl getStore() {
    	return this.store;
    }
    
    public void parseAndDump() throws IOException {
    	this.parseInput();
        DBCollection routesColl, tripsColl;
        
        routesColl = MongoUtils.getDB("ruter").getCollection("routes");
        routesColl.drop();
        routesColl = MongoUtils.getDB().getCollection("routes");
        
        tripsColl = MongoUtils.getDB("ruter").getCollection("trips");
        tripsColl.drop();
        tripsColl = MongoUtils.getDB().getCollection("trips");
        

        
        this.processRoutes(routesColl);
        this.processDates();
        this.processTrips();
        this.processStops();
        
        Set<String> keySet = trips.keySet();
        int i=0;
        
        for(String tripId: keySet) {
        	BusTrip trip = trips.get(tripId);
        	tripsColl.insert(trip.toMongoObj());
        	i++;
        	System.out.println("Imported " + i + " trips out of "+keySet.size() + " \r");
        }
        
        System.out.println("Imported " + trips.size() + " bus trips");
    }

	private void processStops() {
		for(StopTime st: store.getAllStopTimes()) {
        	BusStop busStop;
        	BusTrip busTrip = trips.get(st.getTrip().getId().getId());
        	String stopId = st.getStop().getId().getId();

        	if(busTrip == null) {
        		continue;
        	}
        	
        	if(stops.containsKey(stopId)) {
        		busStop = stops.get(stopId);
        	} else {
        		Stop stop = st.getStop();
        		busStop = new BusStop(stop);
        		stops.put(busStop.getStopId(), busStop);
        	}
        	
        	ScheduleStop scheduleStop = new ScheduleStop(st, busStop);
        	busTrip.addStop(scheduleStop);
        }
		
		System.out.println("Detected "+ stops.size() + " bus stops");
	}

	private void processTrips() {
		for(Trip t: store.getAllTrips()) {
            BusTrip trip = new BusTrip();
            trip.setDates(opDates.get(t.getServiceId().getId()));
            trip.setDataSource("ruter");
            String tripId = t.getId().getId();
            
            trip.setServiceID(tripId);
            trip.setFootnoteId(t.getServiceId().getId()); //references service id in calendar dates
            trip.setRoute(t.getRoute().getShortName());
            trip.setServiceNbr(t.getRoute().getId().getId());
            trip.setDirection(t.getDirectionId());
            trips.put(tripId, trip);
        }
	}

	private void processDates() {
		for(ServiceCalendarDate scd: this.store.getAllCalendarDates()) {
        	//Service is not operating on that day
        	if(scd.getExceptionType() == 2) {
        		continue;
        	}
        	
        	String serviceId = scd.getServiceId().getId();
        	
        	if(opDates.containsKey(serviceId)) {
        		opDates.get(serviceId).add(scd);
        	} else {
        		List<ServiceCalendarDate> list = new ArrayList<ServiceCalendarDate>();
        		list.add(scd);
            	opDates.put(serviceId, list);
        	}
        }
	}

	private void processRoutes(DBCollection routesColl) {
		for(Route r: this.store.getAllRoutes()) {
            if(r.getType() == BUS_ROUTE_TYPE) {
                BasicDBObject route = new BasicDBObject();
                route.append("id", r.getId().getId())
                .append("description", r.getDesc())
                .append("name", r.getShortName());
                
                routesColl.insert(route);
                routes.put(r.getId().getId().toString(), r);
            }
        }

        System.out.println("Imported " + routes.size() + " routes");
	}    
}
