package nova.master.api.messages;

public class RegisterApplianceMessage {
	public RegisterApplianceMessage() {

	}

	public RegisterApplianceMessage(String displayName, String fileName,
			String osFamily, String description) {
		this.displayName = displayName;
		this.fileName = fileName;
		this.osFamily = osFamily;
		this.description = description;
	}

	public String displayName, fileName;
	public String osFamily, description;

}
