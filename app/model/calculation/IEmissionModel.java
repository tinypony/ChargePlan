package model.calculation;

import java.util.Map;

import model.dataset.BusRoute;
import model.dataset.DayStat;

public interface IEmissionModel {
	public Map<String, Double> getDailyEmissions(BusRoute r, DayStat stat);
}
