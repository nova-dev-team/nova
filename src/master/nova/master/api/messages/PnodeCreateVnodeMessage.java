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

    public PnodeCreateVnodeMessage(String PnodeIP, long VnodeId, int VnodePort) {
        this.PnodeIP = PnodeIP;
        this.VnodeId = VnodeId;
        this.VnodePort = VnodePort;

    }

    public String PnodeIP;

    public long VnodeId;

    public int VnodePort;
}
