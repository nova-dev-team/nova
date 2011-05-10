package nova.agent.api.messages;

import nova.common.util.Pair;

/**
 * Set appliance list in master. Use pair to store appliances' name and
 * information
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceListMessage {
	public Pair<String, String>[] appNameAndInfo = null;

	public ApplianceListMessage() {

	}

	public ApplianceListMessage(Pair<String, String>[] apps) {
		this.appNameAndInfo = apps;
	}

	public void setApps(Pair<String, String>[] apps) {
		this.appNameAndInfo = apps;
	}

	public Pair<String, String>[] getApps() {
		return this.appNameAndInfo;
	}
}
