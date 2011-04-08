package nova.agent.core.service;

import java.net.InetSocketAddress;

import nova.agent.core.handler.SoftwareInstallStatusMessageHandler;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestHeartbeatMessage;

/**
 * The proxy that handles other requests
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class IntimeProxy extends SimpleProxy {
	public IntimeProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendHeartbeatMessage() {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendRequestHeartbeatMessage() {
		this.sendRequest(new RequestHeartbeatMessage());
	}

	public void sendGeneralMonitorMessage() {
		this.sendRequest(new GeneralMonitorMessage());
	}

	public void sendSoftwareInstallStatusMessage() {
		this.sendRequest(new SoftwareInstallStatusMessageHandler());
	}

	public void sendCloseChannelMessage() {
		this.sendRequest(new CloseChannelMessage());
	}
}
