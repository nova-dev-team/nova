package nova.worker.daemons;

import nova.common.util.SimpleDaemon;

/**
 * Daemon thread that reports all current vnodes status to master node.
 * 
 * @author santa
 * 
 */
public class ReportVnodeStatusDeamon extends SimpleDaemon {

	@Override
	protected void workOneRound() {

		// TODO @shayf report actual vnodes status to master
	}

}
