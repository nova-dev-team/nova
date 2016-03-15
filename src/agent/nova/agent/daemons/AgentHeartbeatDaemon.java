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
        MasterProxy master = NovaAgent.getInstance().getMaster();
        if (master != null) {
            if (master.isConnected() == false) {
                NovaAgent.getInstance().registerMaster(NovaAgent.masteraddr);
            }
            NovaAgent.getInstance().getMaster().sendAgentHeartbeat();
            System.out.println("AgentHeartBeatDaemon");
        }
    }

}
