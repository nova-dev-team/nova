package nova.agent.core.service;

import java.net.InetSocketAddress;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.protocol.HeartbeatProtocol;

/**
 * The proxy that handles heartbeat message.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
// TODO @gaotao extract these proxies as protocols
public class HeartbeatProxy extends SimpleProxy implements HeartbeatProtocol {

	public HeartbeatProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	@Override
	public void sendHeartbeat() {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendCloseChannelMessage() {
		this.sendRequest(new CloseChannelMessage());
	}

}
