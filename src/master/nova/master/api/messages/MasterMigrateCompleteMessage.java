package nova.master.api.messages;

public class MasterMigrateCompleteMessage {

    public MasterMigrateCompleteMessage() {

    }

    public MasterMigrateCompleteMessage(String migrateUuid, String dstPnodeIP,
            String strVNCPort) {
        this.migrateUuid = migrateUuid;
        this.dstPnodeIP = dstPnodeIP;
        this.dstVNCPort = strVNCPort;
    }

    public String migrateUuid;
    public String dstPnodeIP;
    public String dstVNCPort;
}
