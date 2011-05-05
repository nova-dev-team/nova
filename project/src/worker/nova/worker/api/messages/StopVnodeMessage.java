package nova.worker.api.messages;

/**
 * Message for "stop an existing vnode" request.
 * 
 * @author shayf
 * 
 */
public class StopVnodeMessage {

	public String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public StopVnodeMessage(String uuid) {
		this.uuid = uuid;
	}
}
