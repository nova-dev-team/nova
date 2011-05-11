package nova.master.api.messages;

import nova.master.models.Vcluster;

public class VclusterStatusMessage {

	Vcluster[] vcluster;

	public VclusterStatusMessage() {

	}

	public VclusterStatusMessage(Vcluster[] vcluster) {
		this.vcluster = vcluster;
	}

}
