package nova.master.daemons;

import org.apache.log4j.Logger;

import nova.common.util.SimpleDaemon;

/**
 * A simple vm scheduling algorithm. Load balancing.
 * 
 * @author Tianyu Chen
 */
public class LoadBalancingDaemon extends SimpleDaemon {

    // Logger
    Logger logger = Logger.getLogger(AutoManagerDaemon.class);

    public LoadBalancingDaemon() {
        // wait 120s for next round
        super(120000);
    }

    @Override
    protected void workOneRound() {
        // 1. find the hotspot of physical machines

        // 2. choose the best vm on the physical node

        // 3. find the destination

        // 4. migrate
    }
}
