package model;

import java.util.List;

import model.sql.BusStop;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.DBRef;

@Entity
public class RouteGroup {
	public String serviceNbr;
	public String direction;
	
	@JsonIgnore
	@Reference
	public List<DBRef> referencedStops;
	public List<BusStop> stops;
	public RouteGroup(){}
	
	
}
