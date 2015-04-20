package configuration.emn.route;

import model.dataset.ScheduleStop;

public interface StopsDistanceRetriever {
	public int getDistanceBetweenStops(ScheduleStop a, ScheduleStop b) throws Exception;
}
