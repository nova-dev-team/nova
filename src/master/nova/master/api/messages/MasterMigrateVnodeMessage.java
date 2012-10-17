package nova.master.api.messages;

public class MasterMigrateVnodeMessage {

    public MasterMigrateVnodeMessage() {
    }

    public MasterMigrateVnodeMessage(long vnodeId, long migrateFrom,
            long migrateTo) {
        this.vnodeId = vnodeId;
        this.migrateFrom = migrateFrom;
        this.migrateTo = migrateTo;
    }

    public long vnodeId, migrateFrom, migrateTo;

}
