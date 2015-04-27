package org.emn.calculate.bus;

import java.util.Map;

import model.planning.solutions.ElectricBus;

public class StaticConsumptionProfile implements IConsumptionProfile {

	private double consumption;
	
	
	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	@Override
	public double getConsumption(ElectricBus bus, Map<String, Double> params) {
		return this.getConsumption();
	}

}
