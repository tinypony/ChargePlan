package model.calculation;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
	private boolean survived;
	private List<BatteryStateEntry> batteryHistory;
	
	public SimulationResult() {
		setBatteryHistory(new ArrayList<BatteryStateEntry>());
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

	public void setBatteryHistory(List<BatteryStateEntry> batteryHistory) {
		this.batteryHistory = batteryHistory;
	}
	
	public void addBatteryStateEntry(BatteryStateEntry en) {
		this.batteryHistory.add(en);
	}
}
