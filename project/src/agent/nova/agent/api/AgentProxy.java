package nova.agent.api;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;

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
		super.sendRequest(new HeartbeatMessage());
	}

	public void sendCloseChannelRequest() {
		super.sendRequest(new CloseChannelMessage());
	}

	public void sendSoftwareList(LinkedList<String> installSoftList) {
		super.sendRequest(new QueryApplianceStatusMessage(installSoftList));
	}

	public void sendRequestMonitorInfo() {
		super.sendRequest(new QueryPerfMessage());
	}

	public void sendRequestHeartbeat() {
		super.sendRequest(new QueryHeartbeatMessage());
	}

}
