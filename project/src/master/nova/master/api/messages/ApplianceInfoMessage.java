package nova.master.api.messages;

import nova.agent.appliance.Appliance;

public class ApplianceInfoMessage {

	Appliance[] appList;

	public ApplianceInfoMessage(Appliance[] appList) {
		this.appList = appList;
	}

}
