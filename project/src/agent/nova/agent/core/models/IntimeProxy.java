package nova.agent.core.models;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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
		try {
			this.sendRequest(new HeartbeatMessage());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendRequestHeartbeatMessage() {
		try {
			this.sendRequest(new RequestHeartbeatMessage());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendGeneralMonitorMessage() {
		try {
			this.sendRequest(new GeneralMonitorMessage());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendSoftwareInstallStatusMessage() {
		try {
			this.sendRequest(new SoftwareInstallStatusMessageHandler());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendCloseChannelMessage() {
		try {
			this.sendRequest(new CloseChannelMessage());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
