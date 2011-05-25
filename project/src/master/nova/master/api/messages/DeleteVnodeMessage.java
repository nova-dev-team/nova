package nova.master.api.messages;

public class DeleteVnodeMessage {

	public DeleteVnodeMessage() {

	}

	public DeleteVnodeMessage(long id) {
		this.id = id;
	}

	public long id;

}
