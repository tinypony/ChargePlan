package model.calculation;

import java.util.HashMap;
import java.util.Map;

import model.dataset.BusRoute;
import model.dataset.DayStat;

public class Euro6EmissionModel implements IEmissionModel {
	
	// http://www2.vtt.fi/inf/pdf/tiedotteet/2007/T2373.pdf
    // http://www.embarq.org/sites/default/files/Exhaust-Emissions-Transit-Buses-EMBARQ.pdf
    // https://www.google.fi/url?sa=t&rct=j&q=&esrc=s&source=web&cd=5&cad=rja&uact=8&ved=0CDsQFjAE&url=http%3A%2F%2Fwww.researchgate.net%2Fprofile%2FChristopher_Koroneos%2Fpublication%2F262193381_Comparative_environmental_assessment_of_Athens_urban_busesDiesel_CNG_and_biofuel_powered%2Flinks%2F0deec53b17a7cf2680000000.pdf&ei=pUn4VJGnI8b_ywPEoIKQDg&usg=AFQjCNEMzSKvhrJE6KkapaImHX9JZso6cA&sig2=Qg3BQ3SCqIiNdkurSfAwnw&bvm=bv.87519884,d.bGQ
	private static final double CO2_KG_PER_KM = 1.330;
	private static final double CO_KG_PER_KM = 0.006;
	private static final double NOx_KG_PER_KM = 0.003;
	
	@Override
	public Map<String, Double> getDailyEmissions(BusRoute r, DayStat stat) {
		Map<String, Double> emissions = new HashMap<String, Double>();
		emissions.put("CO2", CO2_KG_PER_KM * stat.getTotalDistance()/1000);
		emissions.put("CO", CO_KG_PER_KM * stat.getTotalDistance()/1000);
		emissions.put("NOx", NOx_KG_PER_KM * stat.getTotalDistance()/1000);
		return emissions;
	}

}
