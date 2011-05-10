package nova.agent.daemons;

import java.io.IOException;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.ApplianceInstaller;
import nova.common.util.SimpleDaemon;

import org.apache.log4j.Logger;

/**
 * Daemon deal with downloading softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceInstallDaemon extends SimpleDaemon {

	Logger log = Logger.getLogger(ApplianceInstallDaemon.class);

	public ApplianceInstallDaemon() {
		super(100);
	}

	@Override
	protected void workOneRound() {

		for (Appliance app : NovaAgent.getInstance().getAppliances().values()) {
			if (app.getStatus().equals(Appliance.Status.INSTALL_PENDING)) {
				log.info("Found INSTALL_PENDING appliance: " + app.getName());
				// Create a new thread to avoid block when Spawn
				new Thread(new InstallThread(app)).start();

			}
		}

	}

}

class InstallThread implements Runnable {
	Logger log = Logger.getLogger(InstallThread.class);

	private Appliance app = null;

	public InstallThread(Appliance app) {
		this.app = app;
	}

	@Override
	public void run() {
		try {
			this.app.setStatus(Appliance.Status.INSTALLING);

			ApplianceInstaller.install(this.app);

			this.app.setStatus(Appliance.Status.INSTALLED);
		} catch (IOException e) {
			log.error("Install failure!", e);
			this.app.setStatus(Appliance.Status.INSTALL_FAILURE);
		}
	}
}
