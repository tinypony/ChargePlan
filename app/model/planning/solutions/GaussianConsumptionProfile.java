package model.planning.solutions;

import java.util.Map;
import java.util.Random;

public class GaussianConsumptionProfile implements IConsumptionProfile {
	
	private double mean;
	private double variance;
	
	private Random gen;
	
	public GaussianConsumptionProfile() {
		this.gen = new Random();
	}
	
	public void setParams(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
	}
	
	@Override
	public double getConsumption(ElectricBus bus, Map<String, Double> params) {
		return this.mean + gen.nextGaussian() * this.variance;
	}

}