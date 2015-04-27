import org.emn.calculate.route.Euro6EmissionModel;
import org.emn.calculate.route.IEmissionModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import model.dataset.BusRoute;
import model.dataset.DayStat;


public class EmissionModelTest {

	IEmissionModel model;
	BusRoute route;
	DayStat stat;
	
	@Before
	public void setUp() {
		model = new Euro6EmissionModel();
		route = new BusRoute();
		stat = new DayStat();
	}
	
	@Test
	public void mustSuccessfullySetEmissions() {
		stat.setEmissions(model.getDailyEmissions(route, stat));
		assertEquals(3, stat.getEmissions().entrySet().size());
	}
}
