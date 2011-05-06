package nova.agent.daemons;

import nova.agent.appliance.ApplianceFetcher;
import nova.agent.appliance.FtpApplianceFetcher;
import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with installing softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PackageInstallDaemon extends SimpleDaemon {

	ApplianceFetcher fetcher = null;

	private PackageInstallDaemon() {
		super(100);
		fetcher = new FtpApplianceFetcher();
	}

	@Override
	protected void workOneRound() {
		// TODO @santa
	}

	public synchronized void markInstall(String appName) {
		// TODO @santa
	}

	private static PackageInstallDaemon instance = null;

	public static synchronized PackageInstallDaemon getInstance() {
		if (PackageInstallDaemon.instance == null) {
			PackageInstallDaemon.instance = new PackageInstallDaemon();
		}
		return PackageInstallDaemon.instance;
	}

}
