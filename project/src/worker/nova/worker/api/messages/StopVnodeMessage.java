package nova.worker.api.messages;

/**
 * Message for "stop an existing vnode" request.
 * 
 * @author shayf
 * 
 */
public class StopVnodeMessage {

	public String uuid;
	public boolean powerOffOnly;

	public boolean getPowerOffOnly() {
		return powerOffOnly;
	}

	public void setPowerOffOnly(boolean powerOffOnly) {
		this.powerOffOnly = powerOffOnly;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public StopVnodeMessage(String uuid) {
		this.uuid = uuid;
		this.powerOffOnly = false;
	}

	public StopVnodeMessage(String uuid, boolean powerOffOnly) {
		this.uuid = uuid;
		this.powerOffOnly = powerOffOnly;
	}
}
