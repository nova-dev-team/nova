package nova.agent.core.service;

import java.net.InetSocketAddress;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;

/**
 * The proxy that handles monitor information message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
// TODO @gaotao extract these proxies as protocols
public class GeneralMonitorProxy extends SimpleProxy {

	public GeneralMonitorProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendGeneralMonitorMessage() {
		this.sendRequest(new GeneralMonitorMessage());
	}

	public void sendCloseChannelMessage() {
		this.sendRequest(new CloseChannelMessage());
	}

}
