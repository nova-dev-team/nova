package nova.test.agent;

import java.io.IOException;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.ApplianceInstaller;

import org.junit.Test;

public class TestInstallAppliance {
	@Test
	public void testInstallAppliance() {
		Appliance app = new Appliance("picture");
		app.setStatus(Appliance.Status.INSTALL_PENDING);
		NovaAgent.getInstance().getAppliances().put("picture", app);
		try {
			ApplianceInstaller.install(app);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
