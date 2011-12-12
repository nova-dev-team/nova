package nova.test.functional.agent;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.Appliance.Status;
import nova.agent.daemons.CheckApplianceFirstInstalledDaemon;

import org.junit.Test;

public class TestCheckFirstInstalled {
    @Test
    public void testCheckFirstInstalled() {
        String[] appsInstalled = { "demo1", "demo2", "demo3" };

        // load apps info in data/appliances/apps.json
        NovaAgent.getInstance().loadAppliances();

        for (Appliance app : NovaAgent.getInstance().getAppliances().values()) {
            app.setStatus(Status.INSTALL_PENDING);
        }
        // Test this Daemon
        new CheckApplianceFirstInstalledDaemon(appsInstalled).start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Daemon will stop
        for (Appliance app : NovaAgent.getInstance().getAppliances().values()) {
            app.setStatus(Status.INSTALLED);
        }
        // Make sure this daemon is stopped correctly
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
