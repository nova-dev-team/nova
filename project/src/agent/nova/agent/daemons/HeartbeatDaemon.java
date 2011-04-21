package nova.agent.daemons;

import nova.agent.api.AgentProxy;
import nova.agent.common.util.GlobalPara;
import nova.common.service.SimpleAddress;
import nova.common.util.SimpleDaemon;

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
		if (!GlobalPara.agentProxyMap.isEmpty()) {
			for (SimpleAddress xreply : GlobalPara.agentProxyMap.keySet()) {
				AgentProxy heartbeatProxy = GlobalPara.agentProxyMap
						.get(xreply);
				heartbeatProxy.sendHeartbeat();
			}
		}
	}

}
