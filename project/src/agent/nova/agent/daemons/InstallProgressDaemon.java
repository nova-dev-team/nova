package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.InstallProgress;
import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with installing softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class InstallProgressDaemon extends SimpleDaemon {

	public InstallProgressDaemon() {
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
