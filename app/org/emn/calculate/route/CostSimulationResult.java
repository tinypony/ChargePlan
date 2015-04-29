package org.emn.calculate.route;

public class CostSimulationResult {

	private int metersDriven;
	private Double energyPrice;
	private Double dieselPrice;
	
	public CostSimulationResult() {
		
	}

	public Double getDieselPrice() {
		return dieselPrice;
	}

	public void setDieselPrice(Double dieselPrice) {
		this.dieselPrice = dieselPrice;
	}

	public int getMetersDriven() {
		return metersDriven;
	}

	public void setMetersDriven(int metersDriven) {
		this.metersDriven = metersDriven;
	}

	public Double getEnergyPrice() {
		return energyPrice;
	}

	public void setEnergyPrice(Double energyPrice) {
		this.energyPrice = energyPrice;
	}
	
	
}
