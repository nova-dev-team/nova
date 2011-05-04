package nova.master.api.messages;

import java.util.UUID;

import nova.master.models.Vnode;

/**
 * A wrapper for {@link UUID} and {@link Vnode.Status}, so they could be send
 * together.
 * 
 * @author santa
 * 
 */
public class VnodeStatusMessage {

	public VnodeStatusMessage(UUID uuid, Vnode.Status status) {
		this.uuid = uuid;
		this.status = status;
	}

	/**
	 * The {@link UUID} for vnode.
	 */
	public UUID uuid;

	/**
	 * The {@link Vnode.Status}.
	 */
	public Vnode.Status status;

}
