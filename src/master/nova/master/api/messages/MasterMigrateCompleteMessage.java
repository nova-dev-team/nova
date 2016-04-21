package nova.master.api.messages;

public class MasterMigrateCompleteMessage {

    public MasterMigrateCompleteMessage() {

    }

    public MasterMigrateCompleteMessage(String migrateUuid, String dstPnodeIP,
            String strVNCPort, String hypervisor) {
        this.migrateUuid = migrateUuid;
        this.dstPnodeIP = dstPnodeIP;
        this.dstVNCPort = strVNCPort;
        this.hypervisor = hypervisor;
    }

    public String migrateUuid;
    public String dstPnodeIP;
    public String dstVNCPort;
    // added by Tianyu Chen
    public String hypervisor;
}
