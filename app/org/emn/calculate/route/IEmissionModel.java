package org.emn.calculate.route;

import java.util.Map;

import model.dataset.BusRoute;
import model.dataset.DayStat;
import model.dataset.aggregation.BusRouteAggregation;

public interface IEmissionModel {
	public Map<String, Double> getDailyEmissions(BusRoute r, DayStat stat);
	public Map<String, Double> getDailyEmissions(BusRouteAggregation agg);
}
