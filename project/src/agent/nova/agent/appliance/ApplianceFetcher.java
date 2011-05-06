package nova.agent.appliance;

import java.io.IOException;

public abstract class ApplianceFetcher {

	public abstract void fetch(String appName) throws IOException;

}
