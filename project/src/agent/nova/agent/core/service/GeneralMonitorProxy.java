package nova.agent.core.service;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;

/**
 * The proxy that handles monitor information message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class GeneralMonitorProxy extends SimpleProxy {

	public GeneralMonitorProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendGeneralMonitorMessage() {
		try {
			this.sendRequest(new GeneralMonitorMessage());
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
