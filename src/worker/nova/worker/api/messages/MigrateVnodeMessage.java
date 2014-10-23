package nova.worker.api.messages;

import nova.common.service.SimpleAddress;

public class MigrateVnodeMessage {
    public MigrateVnodeMessage() {
    }

    public MigrateVnodeMessage(String vnodeUuid, SimpleAddress migrateToAddr) {
        this.vnodeUuid = vnodeUuid;
        this.migrateToAddr = migrateToAddr;
        this.migrateToUserName = "username";
        this.migrateToPasswd = "passwd";
    }

    // add by eagle
    public MigrateVnodeMessage(String vnodeUuid, SimpleAddress migrateToAddr,
            String strUserName, String strPasswd) {
        this.vnodeUuid = vnodeUuid;
        this.migrateToAddr = migrateToAddr;
        this.migrateToUserName = strUserName;
        this.migrateToPasswd = strPasswd;
    }

    // eagle--end

    public String vnodeUuid;
    public SimpleAddress migrateToAddr;

    // add by eagle
    public String migrateToUserName;
    public String migrateToPasswd;
    // eagle--end
}
