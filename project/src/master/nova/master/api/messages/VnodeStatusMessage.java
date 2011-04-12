package nova.master.api.messages;

import nova.master.models.Vnode;

/**
 * A wrapper for {@link Vnode.Identity} and {@link Vnode.Status}, so they could
 * be send together.
 * 
 * @author santa
 * 
 */
public class VnodeStatusMessage {

	public VnodeStatusMessage(Vnode.Identity vIdent, Vnode.Status status) {
		this.vIdent = vIdent;
		this.status = status;
	}

	/**
	 * The {@link Vnode.Identity}.
	 */
	public Vnode.Identity vIdent;

	/**
	 * The {@link Vnode.Status}.
	 */
	public Vnode.Status status;

}
