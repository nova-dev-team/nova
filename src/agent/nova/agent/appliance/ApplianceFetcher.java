package nova.agent.appliance;

import java.io.IOException;

public abstract class ApplianceFetcher {

    public abstract void fetch(Appliance app) throws IOException;

}
