package nova.agent.core.models;

import java.net.InetSocketAddress;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;

/**
 * The proxy that handles heartbeat message.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class HeartbeatProxy extends SimpleProxy {

	public HeartbeatProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendHeartbeatMessage() {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendCloseChannelMessage() {
		this.sendRequest(new CloseChannelMessage());
	}
}
