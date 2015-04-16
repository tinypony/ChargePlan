package model.planning.solutions;

import java.util.Map;

public interface IConsumptionProfile {
	public double getConsumption(ElectricBus bus, Map<String, Double> params);
}
