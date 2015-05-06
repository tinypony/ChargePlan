package org.emn.calculate.price;

public class DieselPricingModel {
	static final int LITERS_PER_100_KM = 27;
	static final double LITER_PRICE = 13.5;
	
	public static Double getCost(int metersDriven) {
		double tmp = ( metersDriven / 1000 ) / 100;
		return tmp * LITERS_PER_100_KM * LITER_PRICE;
	}
}
