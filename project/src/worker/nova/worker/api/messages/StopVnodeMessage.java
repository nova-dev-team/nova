package nova.worker.api.messages;

/**
 * Message for "stop an existing vnode" request.
 * 
 * @author shayf
 * 
 */
public class StopVnodeMessage {

	public String hyperVisor;
	public String uuid;
	public boolean suspendOnly;

	public boolean isSuspendOnly() {
		return suspendOnly;
	}

	public void setSuspendOnly(boolean suspendOnly) {
		this.suspendOnly = suspendOnly;
	}

	public String getHyperVisor() {
		return hyperVisor;
	}

	public void setHyperVisor(String hyperVisor) {
		this.hyperVisor = hyperVisor;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public StopVnodeMessage(String hyperVisor, String uuid) {
		this.hyperVisor = hyperVisor;
		this.uuid = uuid;
		this.suspendOnly = false;
	}

	public StopVnodeMessage(String hyperVisor, String uuid, boolean suspendOnly) {
		this.hyperVisor = hyperVisor;
		this.uuid = uuid;
		this.suspendOnly = suspendOnly;
	}
}
