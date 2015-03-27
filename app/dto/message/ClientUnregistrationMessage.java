package dto.message;

public class ClientUnregistrationMessage {

	private String clientId;
	
	public ClientUnregistrationMessage(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}
	
}
