package nova.worker.api.messages;

import nova.common.service.SimpleAddress;

public class MigrateVnodeMessage {
    public MigrateVnodeMessage() {
    }

    public MigrateVnodeMessage(String vnodeUuid, SimpleAddress migrateToAddr) {
        this.vnodeUuid = vnodeUuid;
        this.migrateToAddr = migrateToAddr;
    }

    public String vnodeUuid;
    public SimpleAddress migrateToAddr;
}
