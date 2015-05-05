package dto.jobstate;

public class JobFailure {

	private String message;
	
	public JobFailure(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
