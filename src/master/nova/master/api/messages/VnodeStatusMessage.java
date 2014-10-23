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

    public VnodeStatusMessage() {

    }

    public VnodeStatusMessage(String vnodeIp, String uuid, Vnode.Status status) {
        this.vnodeIp = vnodeIp;
        this.uuid = uuid;
        this.status = status;
    }

    /**
     * The {@link UUID} for vnode.
     */
    public String uuid;

    /**
     * The {@link Vnode.Status}.
     */
    public Vnode.Status status;

    public String vnodeIp;
}
