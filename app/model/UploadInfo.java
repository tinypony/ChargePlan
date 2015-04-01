package model;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class UploadInfo {
	private boolean imported;
	private boolean inProgress;
	private Date date;
	
	public UploadInfo() {
		this.setImported(false);
		this.setDate(new Date());
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
}
