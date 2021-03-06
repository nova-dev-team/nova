package nova.agent.daemons;

import nova.agent.NovaAgent;
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
        MasterProxy master = NovaAgent.getInstance().getMaster();
        if (master != null) {
            if (master.isConnected() == false) {
                NovaAgent.getInstance().registerMaster(NovaAgent.masteraddr);
            }
            System.out.println("AgentPerfInfoDaemon");
            NovaAgent.getInstance().getMaster().sendVnodeMonitorInfo();
        }
    }
}
