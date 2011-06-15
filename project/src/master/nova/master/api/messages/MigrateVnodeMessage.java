package nova.master.api.messages;

public class MigrateVnodeMessage {

	public MigrateVnodeMessage() {
	}

	public MigrateVnodeMessage(String vnodeId, String migrateToPnodeId) {
		this.vnodeId = vnodeId;
		this.migrateToPnodeId = migrateToPnodeId;
	}

	public String vnodeId, migrateToPnodeId;

}
