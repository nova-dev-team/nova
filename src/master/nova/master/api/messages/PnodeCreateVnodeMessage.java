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

    public PnodeCreateVnodeMessage(String PnodeIP, long VnodeId, int VnodePort,
            String uuid, String hypervisor) {
        this.PnodeIP = PnodeIP;
        this.VnodeId = VnodeId;
        this.VnodePort = VnodePort;
        this.vnodeuuid = uuid;
        this.hypervisor = hypervisor;
    }

    public String PnodeIP;
    public String vnodeuuid;
    public String hypervisor;

    public long VnodeId;

    public int VnodePort;
}
