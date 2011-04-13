package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;

/**
 * General monitor information daemon used in agent
 * 
 * @author gaotao1987@gmail.com0
 * 
 */
public class GeneralMonitorDaemon extends SimpleDaemon {
	// Monitor information interval
	public static final long GENERAL_MONITOR_INTERVAL = 5000;

	public GeneralMonitorDaemon() {
		super(GENERAL_MONITOR_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		if (!GlobalPara.generalMonitorProxyMap.isEmpty()) {
			for (SimpleAddress xreply : GlobalPara.generalMonitorProxyMap
					.keySet()) {
				GeneralMonitorProxy generalMonitorProxy = GlobalPara.generalMonitorProxyMap
						.get(xreply);
				generalMonitorProxy.sendGeneralMonitorMessage();
			}
		}
	}

}