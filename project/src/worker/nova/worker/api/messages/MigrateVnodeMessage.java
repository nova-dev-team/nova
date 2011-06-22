package nova.worker.api.messages;

public class MigrateVnodeMessage {
	public MigrateVnodeMessage() {
	}

	public MigrateVnodeMessage(long vnodeId, long migrateTo) {
		this.vnodeId = vnodeId;
		this.migrateTo = migrateTo;
	}

	public long vnodeId, migrateTo;
}
