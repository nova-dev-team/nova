package nova.common.service.protocol;

import nova.common.service.SimpleAddress;
import nova.master.models.Pnode;

public interface PnodeStatusProtocol {

	public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status);

}
