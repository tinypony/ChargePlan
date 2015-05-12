package dto.message.client;

import java.util.HashMap;
import java.util.Map;

public class CostSimulationResult {

	private String routeId;
	private String date;
	private int metersDriven;
	private Double energyPrice;
	private Double dieselPrice;
	private Map<String, Double> emissions;

	public CostSimulationResult() {
		
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public Double getSavings() {
		return this.getDieselPrice() - this.getEnergyPrice();
	}

	public Map<String, Double> getEmissions() {
		return emissions;
	}

	public void setEmissions(Map<String, Double> emissions) {
		this.emissions = emissions;
	}

}
