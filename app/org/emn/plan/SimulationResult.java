package org.emn.plan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SimulationResult {
	private boolean survived;
	private LinkedList<BatteryStateEntry> batteryHistory;
	
	public SimulationResult() {
		this.setBatteryHistory(new LinkedList<BatteryStateEntry>());
	}

	public boolean isSurvived() {
		return survived;
	}

	public void setSurvived(boolean survived) {
		this.survived = survived;
	}

	public List<BatteryStateEntry> getBatteryHistory() {
		return batteryHistory;
	}

	public void setBatteryHistory(LinkedList<BatteryStateEntry> batteryHistory) {
		this.batteryHistory = batteryHistory;
	}
	
	public void addBatteryStateEntry(BatteryStateEntry en) {
		this.batteryHistory.add(en);
	}
	
	public BatteryStateEntry getLastBatteryStateEntry() {
		try {
			return this.batteryHistory.getLast();
		} catch(NoSuchElementException e) {
			return null;
		}
	}
}
