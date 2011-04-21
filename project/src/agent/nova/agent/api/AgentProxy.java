package nova.agent.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.SoftwareInstallStatusMessage;
import nova.common.service.protocol.ClosableProtocol;
import nova.common.service.protocol.HeartbeatProtocol;
import nova.common.service.protocol.MonitorProtocol;
import nova.common.service.protocol.SoftwareProtocol;

public class AgentProxy extends SimpleProxy implements HeartbeatProtocol,
		MonitorProtocol, SoftwareProtocol, ClosableProtocol {

	public AgentProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public AgentProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	@Override
	public void sendSoftwareStatus() {
		super.sendRequest(new SoftwareInstallStatusMessage());
	}

	@Override
	public void sendMonitorInfo() {
		super.sendRequest(new GeneralMonitorMessage());
	}

	@Override
	public void sendHeartbeat() {
		super.sendRequest(new HeartbeatMessage());
	}

	@Override
	public void sendCloseChannelRequest() {
		super.sendRequest(new CloseChannelMessage());
	}

}
