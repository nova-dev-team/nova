package nova.agent.daemons;

import nova.agent.GlobalPara;
import nova.agent.InstallProgress;
import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with installing softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PackageInstallDaemon extends SimpleDaemon {

	public PackageInstallDaemon() {
		super(100);
	}

	@Override
	protected void workOneRound() {

		String downloadedSoftware = GlobalPara.downloadedBuffer.read();
		InstallProgress insP = new InstallProgress(downloadedSoftware,
				GlobalPara.myPath);
		insP.install();
	}
}
