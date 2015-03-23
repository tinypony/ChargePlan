package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity("Routes")
public class BusRoute {
	@Id
	ObjectId id;
	String routeId;
	String name;
	
	@Reference 
	List<BusStop> waypoints = new ArrayList<BusStop>();
}
