package org.emn.calculate.bus;

import java.util.Map;

import org.emn.plan.model.ElectricBus;

public interface IConsumptionProfile {
	public double getConsumption(ElectricBus bus, Map<String, Object> params);
}
