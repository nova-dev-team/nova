package nova.master.api.messages;

public class CreateVclusterMessage {
	public CreateVclusterMessage() {
	}

	public CreateVclusterMessage(String vclusterName, int vclusterSize) {
		System.out.println("CreateVclusterMessage");
		this.vclusterName = vclusterName;
		this.vclusterSize = vclusterSize;
	}

	public String vclusterName;
	public int vclusterSize;

}
