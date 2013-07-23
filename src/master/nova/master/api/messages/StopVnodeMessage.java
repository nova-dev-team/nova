package nova.master.api.messages;

import nova.master.models.Vnode;

public class StopVnodeMessage {
    public String hyperVisor;
    public String uuid;
    public boolean suspendOnly;
    public String pnodid;

    public StopVnodeMessage(String strhyper, long vnodid, boolean sus,
            String pnodeid) {
        this.hyperVisor = strhyper;
        Vnode node = Vnode.findById(vnodid);
        this.uuid = node.getUuid();
        this.suspendOnly = sus;
        this.pnodid = pnodeid;
    }
}
