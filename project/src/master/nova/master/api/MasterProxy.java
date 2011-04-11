package nova.master.api;

import nova.common.service.SimpleProxy;
import nova.common.service.message.HeartbeatMessage;
import nova.common.tools.perf.GeneralMonitorInfo;

/**
 * Proxy for Master node.
 * 
 * @author santa
 * 
 */
public class MasterProxy extends SimpleProxy {

	/**
	 * Report a heartbeat to Master node.
	 */
	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	/**
	 * Send a monitor info to master node.
	 * 
	 * @param info
	 *            Monitor info.
	 */
	public void sendMonitorInfo(GeneralMonitorInfo info) {
		super.sendRequest(info);
	}

}
