package org.emn.calculate.bus;

import java.util.Map;

import org.emn.plan.model.ElectricBus;

public class StaticConsumptionProfile implements IConsumptionProfile {

	private double consumption;
	
	
	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	@Override
	public double getConsumption(ElectricBus bus, Map<String, Object> params) {
		return this.getConsumption();
	}

}
