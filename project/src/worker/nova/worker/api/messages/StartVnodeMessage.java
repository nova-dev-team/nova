package nova.worker.api.messages;

import nova.common.service.SimpleAddress;

/**
 * Message for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeMessage {

	public StartVnodeMessage(SimpleAddress vAddr) {
		this.vAddr = vAddr;
	}

	/**
	 * Basic information required to start a new vnode.
	 */
	public SimpleAddress vAddr;

}
