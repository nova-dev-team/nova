package nova.master.api.messages;

public class MasterInstallApplianceMessage {

	public long aid;
	public String[] appNames = null;

	public MasterInstallApplianceMessage() {

	}

	public MasterInstallApplianceMessage(long aid, String[] appNames) {
		this.aid = aid;
		this.appNames = appNames;
	}

	public void setAppNames(String[] appNames) {
		this.appNames = appNames;
	}

	public String[] getAppNames() {
		return this.appNames;
	}

}
