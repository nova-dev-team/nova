package nova.master.api.messages;

import nova.common.service.SimpleAddress;

public class AddPnodeMessage {

    /**
     * No-arg constructore for gson.
     */
    public AddPnodeMessage() {

    }

    public AddPnodeMessage(SimpleAddress pAddr) {
        this.pAddr = pAddr;
        // System.out.println(pAddr.toString());
    }

    public AddPnodeMessage(SimpleAddress pAddr, int vmcapacity) {
        this.pAddr = pAddr;
        this.vmCapacity = vmcapacity;
    }

    /**
     * The {@link SimpleAddress}.
     */
    public SimpleAddress pAddr;

    public int vmCapacity = 0;

}
