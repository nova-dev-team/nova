package nova.worker.daemons;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
public class ReportVnodeStatusDaemon extends SimpleDaemon {

	/**
	 * Log4j logger.
	 */
	Logger logger = Logger.getLogger(ReportVnodeStatusDaemon.class);

	Map<UUID, Vnode.Status> allStatus = new HashMap<UUID, Vnode.Status>();

	@Override
	protected void workOneRound() {
		// report actual vnodes status to master
		MasterProxy master = NovaWorker.getInstance().getMaster();
		if (this.isStopping() == false && master != null) {
			for (UUID uuid : allStatus.keySet()) {
				Vnode.Status status = allStatus.get(uuid);
				master.sendVnodeStatus(uuid, status);
			}
		}
	}

}
