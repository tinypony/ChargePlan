package controllers;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dto.message.ClientRegistrationMessage;
import dto.message.ClientUnregistrationMessage;
import dto.message.client.JobMonitoringRequest;
import play.libs.F.Callback0;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.WebSocket;
import actors.JobMonitoringActor;


public class JobMonitorController extends Controller {

	public static WebSocket<JsonNode> socket() {
	    return new WebSocket<JsonNode>() {

			@Override
			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				final String uniqueClientId = UUID.randomUUID().toString();
				JobMonitoringActor.actor().tell(new ClientRegistrationMessage(uniqueClientId, out), null);
				
				in.onMessage(new Callback<JsonNode>() {
					@Override
					public void invoke(JsonNode message) throws Throwable {
						ObjectMapper om = new ObjectMapper();
						JobMonitoringRequest request = om.treeToValue(message, JobMonitoringRequest.class);
						request.setClientId(uniqueClientId);
						JobMonitoringActor.actor().tell(request, null);
					}
	            });
				
				in.onClose(new Callback0() {
					@Override
					public void invoke() throws Throwable {
						ClientUnregistrationMessage unreg = new ClientUnregistrationMessage(uniqueClientId);
						JobMonitoringActor.actor().tell(unreg, null);
					}
				});
			}
	    	
	    };
	}
}
