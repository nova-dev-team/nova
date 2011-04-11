package nova.worker.daemons;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.NovaWorker;

/**
 * Deamon thread that sends monitor information to master.
 * 
 * @author santa
 * 
 */
public class MonitorInfoDaemon extends SimpleDaemon {

	/**
	 * Send monitor information.
	 */
	@Override
	protected void workOneRound() {
		if (this.isStopping() == false) {
			MasterProxy master = NovaWorker.getInstance().getMaster();
			if (master != null) {
				GeneralMonitorInfo info = PerfMon.getGeneralMonitorInfo();
				master.sendMonitorInfo(info);
			}
		}
	}

}
