package nova.worker.handler;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.QueryVnodeInfoMessage;
import nova.worker.daemons.VnodeStatusDaemon;

public class WorkerQueryVnodeInfoMessageHandler
        implements SimpleHandler<QueryVnodeInfoMessage> {

    /**
     * TBD need lxc fix in this class. but since this handler is never used in
     * our current version, i don't implement the fix.
     */

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(WorkerQueryVnodeInfoMessageHandler.class);

    @Override
    public void handleMessage(QueryVnodeInfoMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        Vnode.Status vs = null;
        UUID uu = msg.getUuid();

        if (uu != null) {
            vs = VnodeStatusDaemon.allStatus.get(msg.getUuid());
            if (vs == null) {
                synchronized (NovaWorker.getInstance().getConnLock()) {
                    try {
                        Domain dom = NovaWorker.getInstance()
                                .getConn("qemu:///system", false)
                                .domainLookupByUUID(msg.getUuid());
                        if (dom != null) {
                            String info = dom.getInfo().state.toString();
                            if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
                                vs = Vnode.Status.CONNECT_FAILURE;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
                                vs = Vnode.Status.UNKNOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
                                vs = Vnode.Status.PAUSED;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
                                vs = Vnode.Status.RUNNING;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
                                vs = Vnode.Status.SHUTTING_DOWN;
                            } else if (info
                                    .equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
                                vs = Vnode.Status.SHUT_OFF;
                            }
                        } else {
                            dom = NovaWorker.getInstance()
                                    .getConn("vstaros:///system", false)
                                    .domainLookupByUUID(msg.getUuid());
                            if (dom != null) {
                                String info = dom.getInfo().state.toString();
                                if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_BLOCKED")) {
                                    vs = Vnode.Status.PAUSED;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_CRASHED")) {
                                    vs = Vnode.Status.CONNECT_FAILURE;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_NOSTATE")) {
                                    vs = Vnode.Status.UNKNOWN;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_PAUSED")) {
                                    vs = Vnode.Status.PAUSED;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_RUNNING")) {
                                    vs = Vnode.Status.RUNNING;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_SHUTDOWN")) {
                                    vs = Vnode.Status.SHUTTING_DOWN;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_SHUTOFF")) {
                                    vs = Vnode.Status.SHUT_OFF;
                                }
                            }
                        }
                        // NovaWorker.getInstance().closeConnectToKvm();
                    } catch (LibvirtException e1) {
                        log.error("libvirt connection fail", e1);
                    }
                }
            }
            MasterProxy master = NovaWorker.getInstance().getMaster();
            master.sendVnodeStatus(
                    NovaWorker.getInstance().getVnodeIP().get(uu),
                    uu.toString(), vs);
        } else {
            synchronized (NovaWorker.getInstance().getConnLock()) {
                try {
                    if (NovaWorker.getInstance()
                            .getConn("qemu:///system", false)
                            .numOfDomains() > 0) {
                        int[] ids = NovaWorker.getInstance()
                                .getConn("qemu:///system", false).listDomains();
                        for (int i = 0; i < ids.length; i++) {
                            Domain dom = NovaWorker.getInstance()
                                    .getConn("qemu:///system", false)
                                    .domainLookupByID(ids[i]);
                            if (dom != null) {
                                uu = UUID.fromString(dom.getUUIDString());
                                String info = dom.getInfo().state.toString();
                                if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_BLOCKED")) {
                                    vs = Vnode.Status.PAUSED;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_CRASHED")) {
                                    vs = Vnode.Status.CONNECT_FAILURE;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_NOSTATE")) {
                                    vs = Vnode.Status.UNKNOWN;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_PAUSED")) {
                                    vs = Vnode.Status.PAUSED;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_RUNNING")) {
                                    vs = Vnode.Status.RUNNING;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_SHUTDOWN")) {
                                    vs = Vnode.Status.SHUTTING_DOWN;
                                } else if (info.equalsIgnoreCase(
                                        "VIR_DOMAIN_SHUTOFF")) {
                                    vs = Vnode.Status.SHUT_OFF;
                                }
                                // add by eagle
                                VnodeStatusDaemon.allStatus.put(uu, vs);
                                // eagle--end
                            }
                        }
                    }
                    // NovaWorker.getInstance().closeConnectToKvm();
                } catch (LibvirtException ex) {
                    log.error("libvirt connection fail", ex);
                }
            }

            MasterProxy master = NovaWorker.getInstance().getMaster();
            for (UUID uuid : VnodeStatusDaemon.allStatus.keySet()) {
                Vnode.Status status = VnodeStatusDaemon.allStatus.get(uuid);
                master.sendVnodeStatus(
                        NovaWorker.getInstance().getVnodeIP().get(uuid),
                        uuid.toString(), status);
            }
        }
    }
}
