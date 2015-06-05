package configuration.emn.route;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class ApiClasses {

	static final String DISTANCE_API_KEY = "services.external.distance.key";
	static final String API_KEY = play.Configuration.root().getString(DISTANCE_API_KEY);

	public static class DistanceUrl extends GenericUrl {
		public DistanceUrl(String encodedUrl) {
			super(encodedUrl);
		}

		/**
		 * Constructs url to google distance api
		 * 
		 * @return
		 */
		public static DistanceUrl url(String origins, String destinations) {
			return new DistanceUrl(
					"https://maps.googleapis.com/maps/api/distancematrix/json?origins="
							+ origins + "&destinations=" + destinations
							+ "&mode=driving&key=" + API_KEY);
		}

	}

	/**
	 * Google distance api response
	 * 
	 * @author tinypony
	 *
	 */
	public static class DestinationAPIResponse {
		@Key
		private String status;

		@Key
		private List<String> origin_addresses;

		@Key
		private List<String> destination_addresses;

		@Key
		private ArrayList<MatrixRow> rows;
		
		public DestinationAPIResponse(){};
		
		public int calculateTotalDistance() {
			int retval= 0;
			
			for(int i=0; i< rows.size(); i++) {
				MatrixElement matEl = rows.get(i).getElements().get(i);
				if(matEl.status.equalsIgnoreCase("OK")) {
					retval += matEl.distance.value;
				}
			}
			
			return retval;
		}
		
		public String getStatus() {
			return this.status;
		}
	}

	public static class MatrixRow {
		@Key
		private ArrayList<MatrixElement> elements;

		public MatrixRow(){};
		
		public List<MatrixElement> getElements() {
			return this.elements;
		}
	}

	public static class MatrixElement {
		@Key
		private String status;
		
		@Key
		private IntTextPair duration;

		@Key
		private IntTextPair distance;
		
		public MatrixElement(){};
	}

	public static class IntTextPair {
		@Key
		private Integer value;

		@Key
		private String text;
		
		public IntTextPair(){};
	}
}
