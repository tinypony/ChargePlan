package model.calculation;

import java.util.List;
import java.util.Queue;

import model.dataset.BusTrip;
import model.dataset.StopDistance;
import model.planning.ElectrifiedBusStop;
import model.planning.solutions.ElectricBus;

public class RouteSimulationModel {

	private Queue<BusTrip> directionA;
	private Queue<BusTrip> directionB;
	private List<ElectrifiedBusStop> electrifiedStops;
	private List<StopDistance> distances;
	private ElectricBus busType;
	
	public RouteSimulationModel() {
		
	}
	
	public void setDirections(Queue<BusTrip> dirA, Queue<BusTrip> dirB) {
		this.directionA = dirA;
		this.directionB = dirB;
	}

	public Queue<BusTrip> getDirectionA() {
		return directionA;
	}

	public void setDirectionA(Queue<BusTrip> directionA) {
		this.directionA = directionA;
	}

	public Queue<BusTrip> getDirectionB() {
		return directionB;
	}

	public void setDirectionB(Queue<BusTrip> directionB) {
		this.directionB = directionB;
	}

	public List<ElectrifiedBusStop> getElectrifiedStops() {
		return electrifiedStops;
	}

	public void setElectrifiedStops(List<ElectrifiedBusStop> electrifiedStops) {
		this.electrifiedStops = electrifiedStops;
	}

	public List<StopDistance> getDistances() {
		return distances;
	}

	public void setDistances(List<StopDistance> distances) {
		this.distances = distances;
	}

	public ElectricBus getBusType() {
		return busType;
	}

	public void setBusType(ElectricBus busType) {
		this.busType = busType;
	}
	
	public boolean simulate() {
		boolean canRun = true;
		Queue<BusTrip> direction = this.getDirectionA();
		
		while(canRun) {
			BusTrip trip = direction.poll();
			
			if(trip == null) {
				canRun = false;
				break;
			}
		}
		
		return true;
	}
}
