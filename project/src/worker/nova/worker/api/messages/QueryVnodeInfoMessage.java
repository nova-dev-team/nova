package nova.worker.api.messages;

import java.util.UUID;

/**
 * Request vnode information message, if uuid != null, return the queried vnode
 * status, else return all
 */

public class QueryVnodeInfoMessage {
	public QueryVnodeInfoMessage(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	public QueryVnodeInfoMessage() {
		super();
		this.uuid = null;
	}

	UUID uuid;

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
