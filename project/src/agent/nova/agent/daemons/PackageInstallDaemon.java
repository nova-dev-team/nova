package nova.agent.daemons;

import nova.agent.NovaAgent;
import nova.agent.daemons.helper.ApplianceFetcher;
import nova.agent.daemons.helper.FtpApplianceFetcher;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with installing softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PackageInstallDaemon extends SimpleDaemon {

	Conf conf = null;

	ApplianceFetcher fetcher = null;

	private PackageInstallDaemon(Conf conf) {
		super(100);
		this.conf = conf;
		fetcher = new FtpApplianceFetcher(this.conf);
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
			PackageInstallDaemon.instance = new PackageInstallDaemon(NovaAgent
					.getInstance().getConf());
		}
		return PackageInstallDaemon.instance;
	}

}
