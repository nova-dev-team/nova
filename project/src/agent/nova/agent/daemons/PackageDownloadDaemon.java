package nova.agent.daemons;

import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with downloading softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PackageDownloadDaemon extends SimpleDaemon {

	public PackageDownloadDaemon() {
		super(100);
	}

	@Override
	protected void workOneRound() {
		// TODO @santa
	}

}
