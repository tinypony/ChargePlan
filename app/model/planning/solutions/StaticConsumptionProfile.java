package model.planning.solutions;

import java.util.Map;

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
		return params.get("consumption");
	}

}
