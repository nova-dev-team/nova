package nova.master.api.messages;

import nova.common.service.SimpleAddress;
import nova.master.models.Vnode;

/**
 * A wrapper for {@link Vnode.Identity} and {@link Vnode.Status}, so they could
 * be send together.
 * 
 * @author santa
 * 
 */
public class VnodeStatusMessage {

	public VnodeStatusMessage(SimpleAddress vAddr, Vnode.Status status) {
		this.vAddr = vAddr;
		this.status = status;
	}

	/**
	 * The {@link Vnode.Identity}.
	 */
	public SimpleAddress vAddr;

	/**
	 * The {@link Vnode.Status}.
	 */
	public Vnode.Status status;

}
