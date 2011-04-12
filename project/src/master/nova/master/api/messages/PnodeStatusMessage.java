package nova.master.api.messages;

import nova.master.models.Pnode;

/**
 * A wrapper for {@link Pnode.Identity} and {@link Pnode.Status}, so they could
 * be send together.
 * 
 * @author santa
 * 
 */
public class PnodeStatusMessage {

	/**
	 * No-arg constructore for gson.
	 */
	public PnodeStatusMessage() {

	}

	public PnodeStatusMessage(Pnode.Identity pIdent, Pnode.Status status) {
		this.pIdent = pIdent;
		this.status = status;
	}

	/**
	 * The {@link Pnode.Identity}.
	 */
	public Pnode.Identity pIdent;

	/**
	 * The {@link Pnode.Status}.
	 */
	public Pnode.Status status;

}
