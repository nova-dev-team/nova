package nova.master.api.messages;

public class CreateVnodeMessage {

    public CreateVnodeMessage() {
    }

    public CreateVnodeMessage(String vmImage, String vmName, int cpuCount,
            int memorySize, String applianceList, int pnodeId, int ipOffset,
            String vClusterName, boolean is_one, String hypervior) {
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
    }

    public String vmImage, vmName, applianceList, vClusterName, hypervisor;
    public int cpuCount, memorySize;
    public int pnodeId, ipOffset;
    public boolean is_one;

}
