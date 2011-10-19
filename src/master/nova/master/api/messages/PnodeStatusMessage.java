package nova.master.api.messages;

import nova.common.service.SimpleAddress;
import nova.master.models.Pnode;

/**
 * A wrapper for {@link SimpleAddress} and {@link Pnode.Status}, so they could
 * be send together.
 * 
 * @author santa
 * 
 */
public class PnodeStatusMessage {

    /**
     * No-arg constructore for gson.
     */
    public PnodeStatusMessage() {

    }

    public PnodeStatusMessage(SimpleAddress pAddr, Pnode.Status status) {
        this.pAddr = pAddr;
        this.status = status;
    }

    /**
     * The {@link SimpleAddress}.
     */
    public SimpleAddress pAddr;

    /**
     * The {@link Pnode.Status}.
     */
    public Pnode.Status status;

}
