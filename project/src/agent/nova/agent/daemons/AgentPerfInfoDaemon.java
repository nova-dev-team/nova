package nova.agent.daemons;

import nova.agent.GlobalPara;
import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;

/**
 * General monitor information daemon used in agent
 * 
 * @author gaotao1987@gmail.com0
 * 
 */
public class AgentPerfInfoDaemon extends SimpleDaemon {
	/**
	 * Monitor information interval
	 */
	public static final long GENERAL_MONITOR_INTERVAL = 5000;

	public AgentPerfInfoDaemon() {
		super(GENERAL_MONITOR_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		if (!GlobalPara.masterProxyMap.isEmpty()) {
			for (SimpleAddress xreply : GlobalPara.masterProxyMap.keySet()) {
				MasterProxy monitorProxy = GlobalPara.masterProxyMap
						.get(xreply);
				monitorProxy.sendMonitorInfo();
			}
		}
	}

}
