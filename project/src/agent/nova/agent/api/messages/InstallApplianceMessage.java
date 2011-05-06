package nova.agent.api.messages;

/**
 * Install status message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
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
