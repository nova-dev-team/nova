package nova.common.service.protocol;

import java.util.UUID;

import nova.master.models.Vnode.Status;

public interface VnodeStatusProtocol {

	public void sendVnodeStatus(UUID uuid, Status status);

}
