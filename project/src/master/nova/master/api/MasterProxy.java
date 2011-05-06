package nova.master.api;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.PerfMessage;
import nova.master.api.messages.ApplianceStatusMessage;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

/**
 * Proxy for Master node.
 * 
 * @author santa
 * 
 */
public class MasterProxy extends SimpleProxy {

	public MasterProxy(InetSocketAddress bindAddr) {
		super(bindAddr);
	}

	public MasterProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	/**
	 * Report a heartbeat to Master node.
	 */
	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	/**
	 * Send a monitor info to master node.
	 */
	public void sendMonitorInfo() {
		super.sendRequest(new PerfMessage());
	}

	public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status) {
		super.sendRequest(new PnodeStatusMessage(pAddr, status));
	}

	public void sendVnodeStatus(UUID uuid, Vnode.Status status) {
		super.sendRequest(new VnodeStatusMessage(uuid, status));
	}

	public void sendApplianceStatus(Collection<Appliance> appliances) {
		super.sendRequest(new ApplianceStatusMessage((Appliance[]) appliances
				.toArray()));
	}

}
