package nova.worker.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.api.messages.StopVnodeMessage;

/**
 * Connection to worker module.
 * 
 * @author santa
 * 
 */
public class WorkerProxy extends SimpleProxy {

	public WorkerProxy(InetSocketAddress bindAddr) {
		super(bindAddr);
	}

	public WorkerProxy(SimpleAddress replyAddr) {
		super(replyAddr);
	}

	public void sendStartVnode(SimpleAddress vAddr) {
		StartVnodeMessage msg = new StartVnodeMessage(vAddr);
		super.sendRequest(msg);
	}

	public void sendStopVnode(String uuid, boolean powerOffOnly) {
		StopVnodeMessage msg = new StopVnodeMessage(uuid, powerOffOnly);
		super.sendRequest(msg);
	}

	public void sendRequestHeartbeat() {
		super.sendRequest(new QueryHeartbeatMessage());
	}

}
