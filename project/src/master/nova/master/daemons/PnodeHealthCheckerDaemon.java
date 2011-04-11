package nova.master.daemons;

import java.util.ArrayList;

import nova.common.util.SimpleDaemon;
import nova.master.NovaMaster;
import nova.master.models.Pnode;

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
		for (Pnode pnode : allPnodes) {
			if (this.isStopping() == true) {
				// if should stop, cancel as soon as possible
				return;
			}
			logger.trace("pinging pnode: " + pnode);
			// TODO @santa ping pnode if necessary
		}
	}

}
