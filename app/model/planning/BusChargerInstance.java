package model.planning;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import model.planning.solutions.BusCharger;

@Embedded
public class BusChargerInstance {

	@Id
	@JsonSerialize(using=ObjectIdSerializer.class)
	private ObjectId id;
	
	@Embedded
	private BusCharger type;
	private String chargingSchedule;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public BusCharger getType() {
		return type;
	}

	public void setType(BusCharger type) {
		this.type = type;
	}

	public String getChargingSchedule() {
		return chargingSchedule;
	}

	public void setChargingSchedule(String chargingSchedule) {
		this.chargingSchedule = chargingSchedule;
	}
}
