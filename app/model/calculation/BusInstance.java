package model.calculation;

import model.planning.solutions.ElectricBus;
import model.planning.solutions.IConsumptionProfile;

public class BusInstance {

	private ElectricBus type;
	private double batteryState;
	
	public BusInstance(ElectricBus type) {
		this.type = type;
		this.batteryState = type.getCapacity();
	}
	
	public ElectricBus getType() {
		return this.type;
	}
	
	/**
	 * Decreases battery state according to the amount of meters driven and consumption (kWh/km)
	 * @param meters
	 * @param consumption
	 * @return
	 */
	public double drive(int meters, double consumptionKWhperKm) throws IllegalStateException {
		double kWh = (meters/1000.0) * consumptionKWhperKm;
		batteryState -= kWh;
		
		if(batteryState < 0.0){
			throw new IllegalStateException("Battery run empty, oops");
		}
		return batteryState;
	}
	
}
