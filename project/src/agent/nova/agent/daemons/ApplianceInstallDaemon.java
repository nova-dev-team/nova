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
			if (app.getStatus() == Appliance.Status.INSTALL_PENDING) {
				log.info("Found INSTALL_PENDING appliance: " + app.getName());
				try {
					app.setStatus(Appliance.Status.INSTALLING);
					log.info("Installing appliance: " + app.getName());
					ApplianceInstaller.install(app);
					log.info("Appliance installed, mark INSTALLED: "
							+ app.getName());
					app.setStatus(Appliance.Status.INSTALLED);
				} catch (IOException e) {
					log.error(
							"Error installing appliance, set to INSTALL_FAILURE "
									+ app.getName(), e);
					app.setStatus(Appliance.Status.INSTALL_FAILURE);
				}
			}
		}
	}

}
