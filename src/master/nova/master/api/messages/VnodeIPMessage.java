package nova.master.api.messages;

import java.util.UUID;

public class VnodeIPMessage {

    public VnodeIPMessage() {

    }

    public VnodeIPMessage(String vnodeIp, String uuid) {
        this.vnodeIp = vnodeIp;
        this.uuid = uuid;
        // this.status = status;
    }

    /**
     * The {@link UUID} for vnode.
     */
    public String uuid;

    // public Vnode.Status status;

    public String vnodeIp;
}
