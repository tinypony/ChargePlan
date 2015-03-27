package dto.message;


import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.WebSocket;

public class ClientRegistrationMessage {
	public String id;
    public WebSocket.Out<JsonNode> channel;

    public ClientRegistrationMessage(String id, WebSocket.Out<JsonNode> channel) {
        this.id = id;
        this.channel = channel;
    }
}