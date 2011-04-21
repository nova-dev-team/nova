package nova.agent.api;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestGeneralMonitorMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;
import nova.common.service.protocol.ClosableProtocol;
import nova.common.service.protocol.HeartbeatProtocol;
import nova.common.service.protocol.RequestHeartbeatProtocol;
import nova.common.service.protocol.RequestMonitorProtocol;
import nova.common.service.protocol.RequestSoftwareProtocol;

/**
 * Proxy for Agent node.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentProxy extends SimpleProxy implements
		RequestHeartbeatProtocol, RequestMonitorProtocol,
		RequestSoftwareProtocol, HeartbeatProtocol, ClosableProtocol {
	public AgentProxy(InetSocketAddress replyAddr) {
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
		super.sendRequest(new RequestSoftwareMessage(installSoftList));

	}

	@Override
	public void sendRequestMonitorInfo() {
		super.sendRequest(new RequestGeneralMonitorMessage());
	}

	@Override
	public void sendRequestHeartbeat() {
		super.sendRequest(new RequestHeartbeatMessage());
	}

}
