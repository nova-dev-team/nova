package nova.worker.daemons;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;

import org.apache.log4j.Logger;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

/**
 * Daemon thread that reports all current vnodes status to master node.
 * 
 * @author santa
 * 
 */
public class VnodeStatusDaemon extends SimpleDaemon {

    public static final long VNODE_STATUS_INTERVAL = 5000;

    public VnodeStatusDaemon() {
        super(VNODE_STATUS_INTERVAL);
    }

    /**
     * Log4j logger.
     */
    Logger logger = Logger.getLogger(VnodeStatusDaemon.class);

    public static Map<UUID, Vnode.Status> allStatus = new HashMap<UUID, Vnode.Status>();

    public static void putStatus(UUID uu, Vnode.Status vs) {
        allStatus.put(uu, vs);
    }

    public static void delStatus(UUID uu) {
        allStatus.remove(uu);
    }

    @Override
    protected void workOneRound() {
        // TODO @shayf report actual vnodes status to master
        synchronized (NovaWorker.getInstance().getConnLock()) {
            try {
                if (NovaWorker.getInstance().getConn("qemu:///system", false)
                        .numOfDomains() > 0) {
                    // System.out.println("numofdomains\t"
                    // + Integer.toString(NovaWorker.getInstance()
                    // .getConn("qemu:///system", false)
                    // .numOfDomains()));
                    int[] ids = NovaWorker.getInstance()
                            .getConn("qemu:///system", false).listDomains();
                    for (int i = 0; i < ids.length; i++) {
                        Domain dom = NovaWorker.getInstance()
                                .getConn("qemu:///system", false)
                                .domainLookupByID(ids[i]);
                        if (dom != null) {
                            UUID uu = UUID.fromString(dom.getUUIDString());
                            String info = dom.getInfo().state.toString();
                            Vnode.Status vs = null;
                            // TODO @shayf discuss status and finish status enum
                            /*
                             * BOOT_FAILURE?, CONNECT_FAILURE, PREPARING,
                             * RUNNING, SCHEDULING, SHUT_OFF, SHUTTING_DOWN,
                             * PAUSED
                             */
                            if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
                                // the domain is blocked on resource
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
                                // the domain is crashed
                                vs = Vnode.Status.CONNECT_FAILURE;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
                                // the domain has no state
                                vs = Vnode.Status.UNKNOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
                                // the domain is paused by user
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
                                // the domain is running
                                vs = Vnode.Status.RUNNING;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
                                // the domain is being shut down
                                vs = Vnode.Status.SHUTTING_DOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
                                // the domain is shut off
                                vs = Vnode.Status.SHUT_OFF;
                            }
                            allStatus.put(uu, vs);
                        }
                        // NovaWorker.getInstance().closeConnectToKvm();
                    }
                }
                if (NovaWorker.getInstance()
                        .getConn("vstaros:///system", false).numOfDomains() > 0) {
                    int[] ids = NovaWorker.getInstance()
                            .getConn("vstaros:///system", false).listDomains();
                    for (int i = 0; i < ids.length; i++) {
                        Domain dom = NovaWorker.getInstance()
                                .getConn("vstaros:///system", false)
                                .domainLookupByID(ids[i]);
                        if (dom != null) {
                            UUID uu = UUID.fromString(dom.getUUIDString());
                            String info = dom.getInfo().state.toString();
                            Vnode.Status vs = null;
                            if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
                                // the domain is blocked on resource
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
                                // the domain is crashed
                                vs = Vnode.Status.CONNECT_FAILURE;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
                                // the domain has no state
                                vs = Vnode.Status.UNKNOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
                                // the domain is paused by user
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
                                // the domain is running
                                vs = Vnode.Status.RUNNING;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
                                // the domain is being shut down
                                vs = Vnode.Status.SHUTTING_DOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
                                // the domain is shut off
                                vs = Vnode.Status.SHUT_OFF;
                            }
                            allStatus.put(uu, vs);
                        }
                        // NovaWorker.getInstance().closeConnectToKvm();
                    }
                }
                // NovaWorker.getInstance().closeConnectToKvm();

            } catch (LibvirtException e) {
                logger.error("libvirt connection fail", e);
            }
        }

        MasterProxy master = NovaWorker.getInstance().getMaster();

        if (this.isStopping() == false && master != null) {
            for (UUID uuid : allStatus.keySet()) {
                Vnode.Status status = allStatus.get(uuid);
                master.sendVnodeStatus(NovaWorker.getInstance().getVnodeIP()
                        .get(uuid), uuid.toString(), status);
            }
        }
    }
}
