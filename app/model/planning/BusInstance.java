package model.planning;

import org.emn.calculate.bus.IConsumptionProfile;

import model.planning.solutions.ElectricBus;

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
	
	public double getBatteryState() {
		return this.batteryState;
	}
	
	public double getPercentageBatteryState() {
		return 100 *  this.batteryState / this.type.getCapacity();
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
			this.batteryState = 0.0;
			throw new IllegalStateException("Battery run empty, oops");
		}
		return batteryState;
	}

	//time spent charging
	public int charge(int chargingTimeSeconds, double power) {
		double maxTransfered = power * chargingTimeSeconds / (60*60);
		
		if(this.batteryState + maxTransfered <= this.getType().getCapacity()) {
			this.batteryState += maxTransfered;
			return chargingTimeSeconds;
		} else {
			double required = this.type.getCapacity() - this.batteryState;
			this.batteryState = this.getType().getCapacity();
			return (int) Math.round( required * (60*60)/ (power) );
		}
	}
	
}
