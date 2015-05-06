package org.emn.calculate.price;

import java.util.Date;

public interface IEnergyPriceProvider {

	public Double getMWhPrice(Date time);
}
