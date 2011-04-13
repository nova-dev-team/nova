package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.common.util.SimpleDaemon;

/**
 * General monitor information daemon used in agent
 * 
 * @author gaotao1987@gmail.com0
 * 
 */
public class GeneralMonitorDeamon extends SimpleDaemon {
	// Monitor information interval
	public static final long GENERAL_MONITOR_INTERVAL = 5000;

	public GeneralMonitorDeamon() {
		super(GENERAL_MONITOR_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		if (!GlobalPara.generalMonitorProxyMap.isEmpty()) {
			for (String xfrom : GlobalPara.generalMonitorProxyMap.keySet()) {
				GeneralMonitorProxy generalMonitorProxy = GlobalPara.generalMonitorProxyMap
						.get(xfrom);
				generalMonitorProxy.sendGeneralMonitorMessage();
			}
		}
	}

}
