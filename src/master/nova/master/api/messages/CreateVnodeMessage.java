package nova.master.api.messages;

public class CreateVnodeMessage {

    public CreateVnodeMessage() {
    }

    public CreateVnodeMessage(String vmImage, String vmName, int cpuCount,
            int memorySize, String applianceList, int pnodeId, int ipOffset,
            String vClusterName) {
        this.vmImage = vmImage;
        this.vmName = vmName;
        this.cpuCount = cpuCount;
        this.memorySize = memorySize;
        this.applianceList = applianceList;
        this.pnodeId = pnodeId;
        this.ipOffset = ipOffset;
        this.vClusterName = vClusterName;
    }

    public String vmImage, vmName, applianceList, vClusterName;
    public int cpuCount, memorySize;
    public int pnodeId, ipOffset;

}
