package nova.worker.daemons;

import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.NovaWorker;

/**
 * A daemons that sends hearbeat message to Master node.
 * 
 * @author santa
 * 
 */
public class WorkerHeartbeatDaemon extends SimpleDaemon {

    public static final long HEARTBEAT_INTERVAL = 1000;

    public WorkerHeartbeatDaemon() {
        super(HEARTBEAT_INTERVAL);
    }

    /**
     * send heart beat to master
     */
    @Override
    protected void workOneRound() {
        if (this.isStopping() == false) {
            MasterProxy master = NovaWorker.getInstance().getMaster();
            if (master != null) {
                if (master.isConnected() == false
                        && NovaWorker.masteraddr != null) {
                    NovaWorker.getInstance().registerMaster(
                            NovaWorker.masteraddr);
                }
                NovaWorker.getInstance().getMaster().sendPnodeHeartbeat();
            }
        }
    }

}
