package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;

/**
 * Heartbeat daemon used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class HeartbeatDaemon extends SimpleDaemon {
	/**
	 * Heartbeat time interval
	 */
	public static final long HEARTBEAT_INTERVAL = 2000;

	public HeartbeatDaemon() {
		super(HEARTBEAT_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		if (!GlobalPara.masterProxyMap.isEmpty()) {
			for (SimpleAddress xreply : GlobalPara.masterProxyMap.keySet()) {
				MasterProxy heartbeatProxy = GlobalPara.masterProxyMap
						.get(xreply);
				heartbeatProxy.sendHeartbeat();
			}
		}
	}

}
