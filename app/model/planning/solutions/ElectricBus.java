package model.planning.solutions;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("ebuses")
public class ElectricBus {
	@Id
	@JsonSerialize(using=ObjectIdSerializer.class)
	ObjectId id;
	
	String manufacturer;
	String model;
	double capacity;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public double getCapacity() {
		return capacity;
	}
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	
	
}
