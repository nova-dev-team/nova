package nova.worker.daemons;

import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.master.models.Pnode;
import nova.worker.NovaWorker;

public class PnodeStatusDaemon extends SimpleDaemon {

    public static final long PNODE_STATUS_INTERVAL = 2000;

    public PnodeStatusDaemon() {
        super(PNODE_STATUS_INTERVAL);
    }

    @Override
    protected void workOneRound() {
        MasterProxy master = NovaWorker.getInstance().getMaster();
        if (master != null) {
            master.sendPnodeStatus(NovaWorker.getInstance().getAddr(),
                    Pnode.Status.RUNNING);
        }
    }

}
