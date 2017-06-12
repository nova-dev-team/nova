package nova.master.api.messages;

public class CreateVnodeMessage {

    public CreateVnodeMessage() {
    }

    public CreateVnodeMessage(String vmImage, String vmName, int cpuCount,
            int memorySize, String applianceList, int pnodeId, int ipOffset,
            String vClusterName, boolean is_one, String hypervior,
            long user_id, int isvim, int network) {
        this.vmImage = vmImage;
        this.vmName = vmName;
        this.cpuCount = cpuCount;
        this.memorySize = memorySize;
        this.applianceList = applianceList;
        this.pnodeId = pnodeId;
        this.ipOffset = ipOffset;
        this.vClusterName = vClusterName;
        this.is_one = is_one;
        this.hypervisor = hypervior;
        this.user_id = user_id;
        this.isvim = isvim;
        this.network = network;
    }

    public String vmImage, vmName, applianceList, vClusterName, hypervisor;
    public int cpuCount, memorySize;
    public int pnodeId, ipOffset;
    public int isvim;
    public int network;
    public boolean is_one;
    public long user_id;

}
