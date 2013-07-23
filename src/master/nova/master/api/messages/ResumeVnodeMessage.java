package nova.master.api.messages;

public class ResumeVnodeMessage {
    public long pnodeid;
    public long vnodeid;
    public String hypervisor;

    public ResumeVnodeMessage(long pnodeid, long vnodeid, String hypervisor) {
        this.pnodeid = pnodeid;
        this.vnodeid = vnodeid;
        this.hypervisor = hypervisor;
    }
}
