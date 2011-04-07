package nova.master.api;

import nova.common.service.SimpleProxy;
import nova.common.service.message.HeartbeatMessage;
import nova.common.tools.perf.GeneralMonitorInfo;

public class MasterProxy extends SimpleProxy {

	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	public void sendMonitorInfo(GeneralMonitorInfo info) {
		super.sendRequest(info);
	}

}
