package nova.master.api.messages;

/**
 * Message for the worker has created the VM.
 * 
 * @author Cossudy
 * 
 */
public class PnodeCreateVnodeMessage {

    public PnodeCreateVnodeMessage() {

    }

    public PnodeCreateVnodeMessage(int PnodeId, int VnodeId, String uuid) {
        this.PnodeId = PnodeId;
        this.VnodeId = VnodeId;
        this.uuid = uuid;

    }

    public int PnodeId;

    public int VnodeId;

    public String uuid;
}
