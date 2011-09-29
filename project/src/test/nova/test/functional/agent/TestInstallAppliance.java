package nova.test.functional.agent;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.ApplianceFirstInstall;
import nova.agent.appliance.ApplianceInstaller;

import org.junit.Test;

public class TestInstallAppliance {
	@Test
	public void testInstallAppliance() {
		Appliance app = new Appliance("picture");
		app.setStatus(Appliance.Status.INSTALLING);
		NovaAgent.getInstance().getAppliances().put("picture", app);
		ApplianceInstaller.install(app);
		app.setStatus(Appliance.Status.INSTALLED);
	}

	// If you have three directory demo1 blah at E:\ then assert will be false
	@Test
	public void testFirstInstall() {
		String[] softList = { "demo1", "demo2", "demo3" };
		new Thread(new ApplianceFirstInstall(softList)).start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (Appliance app : NovaAgent.getInstance().getAppliances().values()) {
			// Assert not Install
			System.out.println(app.getName() + ": " + app.getStatus());
		}
	}
}
