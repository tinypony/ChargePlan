package configuration.emn.route;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import configuration.emn.MongoUtils;


public class DistanceRetriever {

	static final int MAX_MATRIX_SIDE = 2;
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	static boolean quotaAvailable = true;
	
	static final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
			});

	private static String getOrigins(List<DBObject> stops) {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<stops.size()-1; i++) {
			DBObject stop = stops.get(i);
			if(i == 0) {
				sb.append(stop.get("posY")+","+stop.get("posX"));
			} else {
				sb.append("|"+stop.get("posY")+","+stop.get("posX"));	
			}			
		}
		
		String retval = sb.toString();
		return retval;
	}
	
	private static String getDestinations(List<DBObject> stops) {
		StringBuilder sb = new StringBuilder();
		
		for(int i=1; i<stops.size(); i++) {
			DBObject stop = stops.get(i);
			if(i == 1) {
				sb.append(stop.get("posY")+","+stop.get("posX"));
			} else {
				sb.append("|"+stop.get("posY")+","+stop.get("posX"));	
			}			
		}
		String retval = sb.toString();
		return retval;
	}
	
	public static int getRoutePartLengthOnline(List<DBObject> stops) throws IOException, IllegalStateException {
		ApiClasses.DistanceUrl url = ApiClasses.DistanceUrl.url(getOrigins(stops), getDestinations(stops));

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse response = request.execute();
		ApiClasses.DestinationAPIResponse apiResponse = response.parseAs(ApiClasses.DestinationAPIResponse.class);
		if(apiResponse.getStatus().equals("OVER_QUERY_LIMIT")) {
			throw new IllegalStateException("Too much queries");
		}
		return apiResponse.calculateTotalDistance();
	}
	
	public static int getDistanceBetweenStops(DBObject a, DBObject b) throws IOException, IllegalStateException {
		DBCollection distances 
			= MongoUtils.getDB().getCollection("distances");
		BasicDBObject query = new BasicDBObject();
		query.append("from", a.get("id"))
			.append("to", b.get("id"));
		DBObject result = distances.findOne(query);
		
		if(result != null) {
			return (Integer) result.get("distance");
		} else if(quotaAvailable) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				int retval = getRoutePartLengthOnline(Arrays.asList(a,b));
				
				BasicDBObject newEntry = new BasicDBObject();
				newEntry.append("from", a.get("id"))
					.append("to", b.get("id"))
					.append("distance", retval);
				distances.insert(newEntry);

				return retval;
			} catch(IllegalStateException e) {
				quotaAvailable = false;
				throw e;
			}
		} else {
			throw new IllegalStateException("Quota exceeded");
		}
	}

	public static int getRouteLength(DBObject bus) throws IOException, InterruptedException, IllegalStateException {
		List<DBObject> stopsTotal = (List<DBObject>) bus.get("stops");
		int tmpVal = 0;
		int retval = 0;
		
		for(int j=0; j<stopsTotal.size()-1; j++) {
			DBObject busStopA = stopsTotal.get(j);
			DBObject busStopB = stopsTotal.get(j+1);
			
			tmpVal = getDistanceBetweenStops(busStopA, busStopB);
			retval += tmpVal;
		}
		return retval;
	}
}
