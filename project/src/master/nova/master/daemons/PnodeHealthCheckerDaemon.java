package nova.master.daemons;

import java.util.ArrayList;

import nova.common.util.SimpleDaemon;
import nova.master.NovaMaster;
import nova.master.models.Pnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;

/**
 * Deamon to check each pnode's reachability.
 * 
 * @author santa
 * 
 */
public class PnodeHealthCheckerDaemon extends SimpleDaemon {

	/**
	 * Log4j logger.
	 */
	Logger logger = Logger.getLogger(PnodeHealthCheckerDaemon.class);

	/**
	 * do work, one round
	 */
	@Override
	protected void workOneRound() {
		// ping all pnodes, check their health
		ArrayList<Pnode> allPnodes = NovaMaster.getInstance().getDB()
				.getAllPnodes();
		for (Pnode pnode : allPnodes)
			try {
				if (this.isStopping() == true) {
					// if should stop, cancel as soon as possible
					return;
				}

				if (pnode.getStatus() == Pnode.Status.CONNECT_FAILURE) {
					// skip failed machines
					continue;
				}

				// ping pnode if necessary
				if (pnode.getStatus() == Pnode.Status.RUNNING
						&& pnode.isHeartbeatTimeout() == false) {
					// not timeout, no need to ping
					continue;
				}

				logger.debug("pinging pnode: " + pnode + ", its status="
						+ pnode.getStatus());
				WorkerProxy wp = NovaMaster.getInstance().getWorkerProxy(
						pnode.getAddress());

				// TODO @santa how to detect connection failure here?
				wp.sendRequestHeartbeat();

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
	}
}