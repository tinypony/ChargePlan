package model.calculation;

import java.util.Map;

import model.BusRoute;
import model.DayStat;

public interface IEmissionModel {
	public Map<String, Double> getDailyEmissions(BusRoute r, DayStat stat);
}
