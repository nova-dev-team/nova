package nova.common.service.message;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;

public class GeneralMonitorMessage {
	private GeneralMonitorInfo monitorInfo = new GeneralMonitorInfo();

	public GeneralMonitorMessage() {
		monitorInfo = PerfMon.getGeneralMonitorInfo();
	}

	public GeneralMonitorInfo getGeneralMonitorInfo() {
		return this.monitorInfo;
	}

}
