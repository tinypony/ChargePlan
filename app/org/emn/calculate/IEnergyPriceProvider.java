package org.emn.calculate;

import java.util.Date;

public interface IEnergyPriceProvider {

	public Double getMWhPrice(Date time);
}
