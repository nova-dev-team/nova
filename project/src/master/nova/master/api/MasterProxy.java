package nova.master.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.protocol.HeartbeatProtocol;
import nova.common.service.protocol.MonitorProtocol;
import nova.common.service.protocol.PnodeStatusProtocol;
import nova.common.tools.perf.GeneralMonitorInfo;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.models.Pnode;

/**
 * Proxy for Master node.
 * 
 * @author santa
 * 
 */
public class MasterProxy extends SimpleProxy implements HeartbeatProtocol,
		MonitorProtocol, PnodeStatusProtocol {

	public MasterProxy() {
		super();
	}

	public MasterProxy(InetSocketAddress bindAddr) {
		super(bindAddr);
	}

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

	public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status) {
		super.sendRequest(new PnodeStatusMessage(pAddr, status));
	}

}
