package model.planning.solutions;

import java.util.Map;

public class StaticConsumptionProfile implements IConsumptionProfile {

	@Override
	public double getConsumption(ElectricBus bus, Map<String, Double> params) {
		return params.get("consumption");
	}

}
