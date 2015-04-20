package configuration.emn.route;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import model.dataset.ScheduleStop;
import utils.MongoUtils;

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

public class DistanceRetriever implements StopsDistanceRetriever {

	static final int MAX_MATRIX_SIDE = 2;
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	static boolean quotaAvailable = true;

	static final HttpRequestFactory requestFactory = HTTP_TRANSPORT
			.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
			});

	private String getOrigins(List<ScheduleStop> stops) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < stops.size() - 1; i++) {
			ScheduleStop stop = stops.get(i);
			if (i == 0) {
				sb.append(stop.getStop().getY() + "," + stop.getStop().getX());
			} else {
				sb.append("|" + stop.getStop().getY() + ","
						+ stop.getStop().getX());
			}
		}

		String retval = sb.toString();
		return retval;
	}

	private String getDestinations(List<ScheduleStop> stops) {
		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < stops.size(); i++) {
			ScheduleStop stop = stops.get(i);
			if (i == 1) {
				sb.append(getCoordinateString(stop));
			} else {
				sb.append("|" + getCoordinateString(stop));
			}
		}
		String retval = sb.toString();
		return retval;
	}

	private  String getCoordinateString(ScheduleStop stop) {
		return stop.getStop().getY() + "," + stop.getStop().getX();
	}

	public int getRoutePartLengthOnline(List<ScheduleStop> stops)
			throws IOException, IllegalStateException {
		ApiClasses.DistanceUrl url = ApiClasses.DistanceUrl.url(
				getOrigins(stops), getDestinations(stops));

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse response = request.execute();
		ApiClasses.DestinationAPIResponse apiResponse = response
				.parseAs(ApiClasses.DestinationAPIResponse.class);

		if (apiResponse.getStatus().equals("OVER_QUERY_LIMIT")) {
			throw new IllegalStateException("Too much queries");
		}

		return apiResponse.calculateTotalDistance();
	}

	public int getDistanceBetweenStops(ScheduleStop a, ScheduleStop b)
			throws IOException, IllegalStateException {
		
		DBCollection distances = MongoUtils.getDB().getCollection("distances");
		BasicDBObject query = new BasicDBObject();
		query.append("from", a.getStop().getStopId()).append("to",
				b.getStop().getStopId());
		DBObject result = distances.findOne(query);

		if (result != null) {
			return (Integer) result.get("distance");
		} else if (quotaAvailable) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				int retval = getRoutePartLengthOnline(Arrays.asList(a, b));

				BasicDBObject newEntry = new BasicDBObject();
				newEntry.append("from", a.getStop().getStopId())
						.append("to", b.getStop().getStopId())
						.append("distance", retval);
				distances.insert(newEntry);

				return retval;
			} catch (IllegalStateException e) {
				quotaAvailable = false;
				throw e;
			}
		} else {
			throw new IllegalStateException("Quota exceeded");
		}
	}

	public int getRouteLength(List<ScheduleStop> stopsTotal)
			throws IOException, InterruptedException, IllegalStateException {
		int tmpVal = 0;
		int retval = 0;
		Collections.sort(stopsTotal);

		for (int j = 0; j < stopsTotal.size() - 1; j++) {
			ScheduleStop busStopA = stopsTotal.get(j);
			ScheduleStop busStopB = stopsTotal.get(j + 1);

			tmpVal = getDistanceBetweenStops(busStopA, busStopB);
			retval += tmpVal;
		}
		return retval;
	}
}
