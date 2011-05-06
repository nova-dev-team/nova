package nova.master.api.messages;

import nova.agent.appliance.Appliance;

public class ApplianceStatusMessage {

	Appliance[] appList;

	public ApplianceStatusMessage(Appliance[] appList) {
		this.appList = appList;
	}

}
