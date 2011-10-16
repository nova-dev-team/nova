package nova.master.api.messages;

public class MasterMigrateCompleteMessage {

    public MasterMigrateCompleteMessage() {

    }

    public MasterMigrateCompleteMessage(String migrateUuid) {
        this.migrateUuid = migrateUuid;
    }

    public String migrateUuid;
}
