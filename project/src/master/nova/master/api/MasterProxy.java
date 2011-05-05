package nova.master.api;

import java.net.InetSocketAddress;
import java.util.UUID;

import nova.agent.api.messages.InstallApplianceMessage;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.PerfMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.protocol.HeartbeatProtocol;
import nova.common.service.protocol.MonitorProtocol;
import nova.common.service.protocol.PnodeStatusProtocol;
import nova.common.service.protocol.SoftwareProtocol;
import nova.common.service.protocol.VnodeStatusProtocol;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode.Status;

/**
 * Proxy for Master node.
 * 
 * @author santa
 * 
 */
public class MasterProxy extends SimpleProxy implements HeartbeatProtocol,
		MonitorProtocol, PnodeStatusProtocol, SoftwareProtocol,
		VnodeStatusProtocol {

	public MasterProxy(InetSocketAddress bindAddr) {
		super(bindAddr);
	}

	public MasterProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	/**
	 * Report a heartbeat to Master node.
	 */
	@Override
	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	/**
	 * Send a monitor info to master node.
	 */
	@Override
	public void sendMonitorInfo() {
		super.sendRequest(new PerfMessage());
	}

	@Override
	public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status) {
		super.sendRequest(new PnodeStatusMessage(pAddr, status));
	}

	@Override
	public void sendSoftwareStatus() {
		super.sendRequest(new InstallApplianceMessage());
	}

	@Override
	public void sendVnodeStatus(UUID uuid, Status status) {
		super.sendRequest(new VnodeStatusMessage(uuid, status));
	}

}
