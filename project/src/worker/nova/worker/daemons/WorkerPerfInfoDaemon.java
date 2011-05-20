package nova.worker.daemons;

import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.NovaWorker;

/**
 * Deamon thread that sends monitor information to master.
 * 
 * @author santa
 * 
 */
public class WorkerPerfInfoDaemon extends SimpleDaemon {

	public static final long PERF_INFO_INTERVAL = 2000;

	public WorkerPerfInfoDaemon() {
		super(PERF_INFO_INTERVAL);
	}

	/**
	 * Send monitor information.
	 */
	@Override
	protected void workOneRound() {
		if (this.isStopping() == false) {
			MasterProxy master = NovaWorker.getInstance().getMaster();
			if (master != null) {
				master.sendMonitorInfo();
			}
		}
	}

}
