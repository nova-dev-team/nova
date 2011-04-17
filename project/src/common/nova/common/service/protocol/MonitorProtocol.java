package nova.common.service.protocol;

import nova.common.tools.perf.GeneralMonitorInfo;

public interface MonitorProtocol {

	public void sendMonitorInfo(GeneralMonitorInfo info);

}
