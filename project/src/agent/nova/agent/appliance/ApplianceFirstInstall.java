package nova.agent.appliance;

import java.util.concurrent.ConcurrentHashMap;

import nova.agent.NovaAgent;

/**
 * Install appliances when one vm first start up
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceFirstInstall implements Runnable {

	private String[] appsInstall;

	public ApplianceFirstInstall(String[] args) {
		this.appsInstall = args;
	}

	@Override
	public void run() {
		ConcurrentHashMap<String, Appliance> appliancesMaster = NovaAgent
				.getInstance().getAppliances();
		for (String appName : appsInstall) {
			if (appliancesMaster.containsKey(appName)) {
				Appliance app = appliancesMaster.get(appName);
				app.setStatus(Appliance.Status.INSTALL_PENDING);
				ApplianceInstaller.firstInstall(app);
			} else {
				Appliance app = new Appliance(appName);
				appliancesMaster.put(appName, app);
				app.setStatus(Appliance.Status.INSTALL_PENDING);
				ApplianceInstaller.firstInstall(app);

			}
		}

		NovaAgent.getInstance().saveAppliances();
	}
}
