package nova.master.api;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.common.service.message.PerfMessage;
import nova.common.service.message.PnodeHeartbeatMessage;
import nova.common.service.message.VnodeHeartbeatMessage;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.ApplianceInfoMessage;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.api.messages.RegisterVdiskMessage;
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
	public void sendPnodeHeartbeat() {
		super.sendRequest(new PnodeHeartbeatMessage());
	}

	public void sendVnodeHeartbeat() {
		super.sendRequest(new VnodeHeartbeatMessage());
	}

	public void sendAgentHeartbeat() {
		super.sendRequest(new AgentHeartbeatMessage());
	}

	/**
	 * Send a monitor info to master node.
	 */
	public void sendMonitorInfo() {
		super.sendRequest(new PerfMessage());
	}

	public void sendAddPnode(SimpleAddress pAddr) {
		super.sendRequest(new AddPnodeMessage(pAddr));
	}

	public void sendCreateVnode(String vmImage, String vmName, int cpuCount,
			int memorySize, String applianceList) {
		super.sendRequest(new CreateVnodeMessage(vmImage, vmName, cpuCount,
				memorySize, applianceList));
	}

	public void sendCreateVcluster(String vclusterName, int vclusterSize) {
		super.sendRequest(new CreateVclusterMessage(vclusterName, vclusterSize));
	}

	public void SendRegisterVdisk(String displayName, String fileName,
			String imageType, String osFamily, String osName, String description) {
		super.sendRequest(new RegisterVdiskMessage(displayName, fileName,
				imageType, osFamily, osName, description));
	}

	public void SendRegisterAppliance(String displayName, String fileName,
			String osFamily, String description) {
		super.sendRequest(new RegisterApplianceMessage(displayName, fileName,
				osFamily, description));
	}

	public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status) {
		super.sendRequest(new PnodeStatusMessage(pAddr, status));
	}

	public void sendVnodeStatus(UUID uuid, Vnode.Status status) {
		super.sendRequest(new VnodeStatusMessage(uuid, status));
	}

	public void sendApplianceStatus(Collection<Appliance> appliances) {
		super.sendRequest(new ApplianceInfoMessage((Appliance[]) appliances
				.toArray()));
	}

}
