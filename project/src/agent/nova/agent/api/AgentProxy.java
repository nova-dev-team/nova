package nova.agent.api;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.protocol.ClosableProtocol;
import nova.common.service.protocol.HeartbeatProtocol;
import nova.common.service.protocol.QueryHeartbeatProtocol;
import nova.common.service.protocol.QueryMonitorProtocol;
import nova.common.service.protocol.QueryApplianceStatusProtocol;

/**
 * Proxy for Agent node.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentProxy extends SimpleProxy implements
		QueryHeartbeatProtocol, QueryMonitorProtocol,
		QueryApplianceStatusProtocol, HeartbeatProtocol, ClosableProtocol {
	public AgentProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public AgentProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	@Override
	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	@Override
	public void sendCloseChannelRequest() {
		super.sendRequest(new CloseChannelMessage());
	}

	@Override
	public void sendSoftwareList(LinkedList<String> installSoftList) {
		super.sendRequest(new QueryApplianceStatusMessage(installSoftList));

	}

	@Override
	public void sendRequestMonitorInfo() {
		super.sendRequest(new QueryPerfMessage());
	}

	@Override
	public void sendRequestHeartbeat() {
		super.sendRequest(new QueryHeartbeatMessage());
	}

}
