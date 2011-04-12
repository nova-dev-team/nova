package nova.worker.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleProxy;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.master.models.Vnode;
import nova.worker.handler.StartVnodeHandler;

/**
 * Connection to worker module.
 * 
 * @author santa
 * 
 */
public class WorkerProxy extends SimpleProxy {

	public WorkerProxy() {
		super();
	}

	public WorkerProxy(InetSocketAddress bindAddr) {
		super(bindAddr);
	}

	public void sendStartVnode(Vnode.Identity vIdent) {
		StartVnodeHandler.Message msg = new StartVnodeHandler.Message(vIdent);
		super.sendRequest(msg);
	}

	public void sendRequestHeartbeat() {
		super.sendRequest(new RequestHeartbeatMessage());
	}

}
