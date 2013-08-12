package nova.master.daemons;

import java.util.List;

import nova.common.util.SimpleDaemon;
import nova.master.NovaMaster;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;

/**
 * Deamon to check each pnode's reachability.
 * 
 * @author santa
 * 
 */
public class PnodeCheckerDaemon extends SimpleDaemon {

    /**
     * Log4j logger.
     */
    Logger logger = Logger.getLogger(PnodeCheckerDaemon.class);

    public PnodeCheckerDaemon() {
        super(2000);
    }

    /**
     * do work, one round
     */
    @Override
    protected void workOneRound() {
        // ping all pnodes, check their health
        List<Pnode> allPnodes = Pnode.all();
        for (Pnode pnode : allPnodes)
            try {
                if (this.isStopping() == true) {
                    // if should stop, cancel as soon as possible
                    return;
                }

                if (pnode.getStatus() == Pnode.Status.CONNECT_FAILURE) {
                    // skip failed machines
                    // TODO @santa still need to contact failed machines
                    // continue;
                }

                // ping pnode if necessary
                if (pnode.getStatus() == Pnode.Status.RUNNING
                        && pnode.isHeartbeatTimeout() == false) {
                    // not timeout, no need to ping
                    continue;
                }

                if (pnode.needNewPingMessage() == false) {
                    continue;
                }

                logger.debug("pinging pnode: " + pnode + ", its status="
                        + pnode.getStatus());
                WorkerProxy wp = NovaMaster.getInstance().getWorkerProxy(
                        pnode.getAddr());

                // connection failure could not be detected here, but will be
                // dected in worker proxy's exceptionCaught() handler.
                if (wp.isConnected())
                    wp.sendRequestHeartbeat();
                else {
                    pnode.setStatus(Pnode.Status.CONNECT_FAILURE);
                    pnode.save();

                    List<Vnode> allvnodes = Vnode.all();
                    for (Vnode vnode : allvnodes) {
                        if (vnode.getPmachineId() == pnode.getId()) {
                            vnode.setStatus(Vnode.Status.CONNECT_FAILURE);
                            vnode.save();
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("Error on pnode " + pnode, e);
            }
    }
}
