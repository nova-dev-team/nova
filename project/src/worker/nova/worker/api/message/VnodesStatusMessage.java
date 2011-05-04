package nova.worker.api.message;

import java.util.ArrayList;

import nova.common.service.SimpleAddress;
import nova.master.models.Vnode;

/**
 * all vnodes status message, get by using libvirt-java
 * 
 * @author shayf
 * 
 */
public class VnodesStatusMessage {

	SimpleAddress vAddr;
	ArrayList<Vnode.Status> vnodesStatus;

	VnodesStatusMessage() {

	}

	public VnodesStatusMessage(SimpleAddress vAddr,
			ArrayList<Vnode.Status> vnodesStatus) {
		this.vAddr = vAddr;
		this.vnodesStatus = vnodesStatus;
	}

}
