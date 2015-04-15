package model.planning;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import model.dataset.BusRoute;
import model.dataset.aggregation.BusRouteAggregation;
import model.dataset.aggregation.BusRouteAggregationLight;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ElectrifiedBusRoute extends BusRouteAggregation 
{
	
	public ElectrifiedBusRoute() {
		super();
	}


}
