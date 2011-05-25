package nova.agent.daemons;

import nova.agent.NovaAgent;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;

/**
 * Heartbeat daemon used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentHeartbeatDaemon extends SimpleDaemon {
	/**
	 * Heartbeat time interval
	 */
	public static final long HEARTBEAT_INTERVAL = 1000;

	public AgentHeartbeatDaemon() {
		super(HEARTBEAT_INTERVAL);
	}

	@Override
	protected void workOneRound() {
		MasterProxy proxy = NovaAgent.getInstance().getMaster();
		if (proxy != null) {
			proxy.sendPnodeHeartbeat();
		}
	}

}
