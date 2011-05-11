package nova.master.api.messages;

import nova.master.models.Vdisk;

public class VdiskStatusMessage {

	public Vdisk[] vdisk;

	public VdiskStatusMessage() {

	}

	public VdiskStatusMessage(Vdisk[] vdisk) {
		this.vdisk = vdisk;
	}
}
