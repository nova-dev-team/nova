package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.HeartbeatProxy;
import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;

/**
 * Heartbeat daemon used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class HeartbeatDaemon extends SimpleDaemon {
	// Heartbeat time interval
	public static final long HEARTBEAT_INTERVAL = 2000;

	public HeartbeatDaemon() {
		super(HEARTBEAT_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		if (!GlobalPara.heartbeatProxyMap.isEmpty()) {
			for (SimpleAddress xreply : GlobalPara.heartbeatProxyMap.keySet()) {
				HeartbeatProxy heartbeatProxy = GlobalPara.heartbeatProxyMap
						.get(xreply);
				heartbeatProxy.sendHeartbeatMessage();
			}
		}
	}

}
