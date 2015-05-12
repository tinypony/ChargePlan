package model.dataset;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("transformers")
public class Transformer {
	
	@Id
	@JsonSerialize(using=ObjectIdSerializer.class)
	private ObjectId id;
	private Double lat;
	private Double lon;
	private int voltage;
	
	public Transformer() {
		
	}

	public int getVoltage() {
		return voltage;
	}

	public void setVoltage(int voltage) {
		this.voltage = voltage;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}
	
	
	
	

}
