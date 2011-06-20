package nova.worker.api.messages;

public class InstallApplianceMessage {

	public String[] appNames = null;

	public InstallApplianceMessage() {

	}

	public InstallApplianceMessage(String[] appNames) {
		this.appNames = appNames;
	}

	public void setAppNames(String[] appNames) {
		this.appNames = appNames;
	}

	public String[] getAppNames() {
		return this.appNames;
	}

}
