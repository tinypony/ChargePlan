package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import serialization.ObjectIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity("configs")
public class ClientConfig {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	ObjectId id;

	@Embedded
	UploadInfo schedules;

	@Embedded
	UploadInfo vehicleAssignments;
	
	String recentProject;

	boolean canProceed;

	public ClientConfig() {
		this.schedules = new UploadInfo();
		this.vehicleAssignments = new UploadInfo();
		this.canProceed = false;
	}

	public boolean isSufficient() {
		if (this.schedules.isImported()) {
			this.setCanProceed(true);
			return true;
		} else {
			this.setCanProceed(false);
			return false;
		}
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public UploadInfo getSchedules() {
		return schedules;
	}

	public void setSchedules(UploadInfo schedules) {
		this.schedules = schedules;
	}

	public UploadInfo getVehicleAssignments() {
		return vehicleAssignments;
	}

	public void setVehicleAssignments(UploadInfo vehicleAssignments) {
		this.vehicleAssignments = vehicleAssignments;
	}

	public boolean isCanProceed() {
		return canProceed;
	}

	public void setCanProceed(boolean canProceed) {
		this.canProceed = canProceed;
	}

	public String getRecentProject() {
		return recentProject;
	}

	public void setRecentProject(String recentProject) {
		this.recentProject = recentProject;
	}
	
	
}
