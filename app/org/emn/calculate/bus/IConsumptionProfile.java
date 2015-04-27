package org.emn.calculate.bus;

import java.util.Map;

import model.planning.solutions.ElectricBus;

public interface IConsumptionProfile {
	public double getConsumption(ElectricBus bus, Map<String, Double> params);
}
