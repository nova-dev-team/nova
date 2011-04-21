package nova.worker.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.worker.handler.StartVnodeHandler;

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
		StartVnodeHandler.Message msg = new StartVnodeHandler.Message(vAddr);
		super.sendRequest(msg);
	}

	public void sendRequestHeartbeat() {
		super.sendRequest(new RequestHeartbeatMessage());
	}

}
