package nova.agent.api;

import java.net.InetSocketAddress;

import nova.agent.api.messages.ApplianceListMessage;
import nova.agent.api.messages.InstallApplianceMessage;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.util.Pair;

/**
 * Proxy for Agent node.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentProxy extends SimpleProxy {
	public AgentProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public AgentProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	public void sendHeartbeat() {
		super.sendRequest(new AgentHeartbeatMessage());
	}

	public void sendCloseChannelRequest() {
		super.sendRequest(new CloseChannelMessage());
	}

	public void sendRequestMonitorInfo() {
		super.sendRequest(new QueryPerfMessage());
	}

	public void sendRequestHeartbeat() {
		super.sendRequest(new QueryHeartbeatMessage());
	}

	public void sendInstallAppliance(String... appNames) {
		super.sendRequest(new InstallApplianceMessage(appNames));
	}

	public void sendApplianceList(Pair<String, String>[] apps) {
		super.sendRequest(new ApplianceListMessage(apps));
	}

}
