package nova.worker.api.messages;

/**
 * Message for "stop an existing vnode" request.
 * 
 * @author shayf
 * 
 */
public class StopVnodeMessage {

    String hyperVisor;
    String uuid;
    boolean suspendOnly;
    String pnodid;

    public String getPnodeid() {
        return pnodid;
    }

    public boolean isSuspendOnly() {
        return suspendOnly;
    }

    public void setSuspendOnly(boolean suspendOnly) {
        this.suspendOnly = suspendOnly;
    }

    public String getHyperVisor() {
        return hyperVisor;
    }

    public void setHyperVisor(String hyperVisor) {
        this.hyperVisor = hyperVisor;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StopVnodeMessage() {
    }

    public StopVnodeMessage(String pnodeid, String hyperVisor, String uuid) {
        this.hyperVisor = hyperVisor;
        this.uuid = uuid;
        this.suspendOnly = false;
        this.pnodid = pnodeid;
    }

    public StopVnodeMessage(String pnodeid, String hyperVisor, String uuid,
            boolean suspendOnly) {
        this.pnodid = pnodeid;
        this.hyperVisor = hyperVisor;
        this.uuid = uuid;
        this.suspendOnly = suspendOnly;
    }
}
