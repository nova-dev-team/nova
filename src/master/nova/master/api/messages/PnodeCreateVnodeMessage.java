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

    public PnodeCreateVnodeMessage(String PnodeIP, int VnodeId, String uuid,
            int VnodePort) {
        this.PnodeIP = PnodeIP;
        this.VnodeId = VnodeId;
        this.uuid = uuid;
        this.VnodePort = VnodePort;

    }

    public String PnodeIP;

    public int VnodeId;

    public String uuid;

    public int VnodePort;
}
