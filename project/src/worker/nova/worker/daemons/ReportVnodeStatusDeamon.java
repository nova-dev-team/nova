package nova.worker.daemons;

import java.util.ArrayList;

import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;

import org.apache.log4j.Logger;

/**
 * Daemon thread that reports all current vnodes status to master node.
 * 
 * @author santa
 * 
 */
public class ReportVnodeStatusDeamon extends SimpleDaemon {

	/**
	 * Log4j logger.
	 */
	Logger logger = Logger.getLogger(ReportVnodeStatusDeamon.class);

	SimpleAddress vAddr;
	ArrayList<Vnode.Status> vnodesStatus;

	@Override
	protected void workOneRound() {

		// TODO @shayf report actual vnodes status to master
		if (this.isStopping() == false) {
			MasterProxy master = NovaWorker.getInstance().getMaster();
			if (master != null) {
				master.sendVnodeStatus(vAddr, vnodesStatus);
			}
		}
	}

}
