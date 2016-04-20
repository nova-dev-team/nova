package nova.worker.api.messages;

import nova.common.service.SimpleAddress;

public class MigrateVnodeMessage {
    public MigrateVnodeMessage() {
    }

    public MigrateVnodeMessage(String vnodeUuid, SimpleAddress migrateToAddr,
            String hypervisor) {
        this.vnodeUuid = vnodeUuid;
        this.migrateToAddr = migrateToAddr;
        this.migrateToUserName = "username";
        this.migrateToPasswd = "passwd";

        // added by Tianyu so as to support migration of multiple hypervisors
        this.hypervisor = hypervisor;
    }

    // add by eagle
    public MigrateVnodeMessage(String vnodeUuid, SimpleAddress migrateToAddr,
            String hypervisor, String strUserName, String strPasswd) {
        this.vnodeUuid = vnodeUuid;
        this.migrateToAddr = migrateToAddr;
        this.migrateToUserName = strUserName;
        this.migrateToPasswd = strPasswd;

        // added by Tianyu so as to support migration of multiple hypervisors
        this.hypervisor = hypervisor;
    }
    // eagle--end

    public String vnodeUuid;
    public SimpleAddress migrateToAddr;

    // add by eagle
    public String migrateToUserName;
    public String migrateToPasswd;
    // eagle--end

    // the hypervisor field
    public String hypervisor;
}
