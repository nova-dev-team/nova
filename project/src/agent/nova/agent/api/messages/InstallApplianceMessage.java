package nova.agent.api.messages;

/**
 * Install status message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class InstallApplianceMessage {

	public String appName;

	public InstallApplianceMessage(String appName) {

	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return this.appName;
	}

}
