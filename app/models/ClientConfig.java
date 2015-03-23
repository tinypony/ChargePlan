package models;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("configs")
public class ClientConfig {
	@Id
	ObjectId id;
	
	@Embedded
	UploadInfo schedules;
	
	@Embedded
	UploadInfo vehicleAssignments;
	
	boolean canProceed;
	
	public ClientConfig() {
		this.schedules = new UploadInfo();
		this.vehicleAssignments = new UploadInfo();
		this.canProceed = false;
	}
	
	@Embedded
	private class UploadInfo {
		boolean uploaded;
		Date date;
		
		public UploadInfo() {
			this.uploaded = false;
			this.date = new Date();
		}
	}
}
