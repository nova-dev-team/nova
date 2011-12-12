package nova.master.api.messages;

import nova.common.service.SimpleAddress;

public class AppliancesFirstInstalledMessage {
    public String ipAddress = "";

    public AppliancesFirstInstalledMessage() {

    }

    public AppliancesFirstInstalledMessage(SimpleAddress address) {
        this.ipAddress = address.toString();
    }
}
