package nova.agent.core.service;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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
		try {
			this.sendRequest(new HeartbeatMessage());
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
