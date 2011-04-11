package nova.worker.api;

import nova.common.service.SimpleProxy;
import nova.master.models.VnodeBasic;
import nova.worker.handler.StartVnodeHandler;

/**
 * Connection to worker module.
 * 
 * @author santa
 * 
 */
public class WorkerProxy extends SimpleProxy {

	public void sendStartVnode(VnodeBasic vnodeInfo) {
		StartVnodeHandler.Message msg = new StartVnodeHandler.Message(vnodeInfo);
		super.sendRequest(msg);
	}

}
