package nova.worker.handler;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;

/**
 * destroy and undefine a domain on a physical host
 * 
 * @author shayf, Tianyu Chen
 * 
 */
public class StopVnodeHandler implements SimpleHandler<StopVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(StopVnodeHandler.class);

    @Override
    public void handleMessage(StopVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        final String virtService;
        if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
            virtService = "qemu:///system";
        } else if (msg.getHyperVisor().equalsIgnoreCase("vstaros")) {
            virtService = "vstaros:///system";
        } else if (msg.getHyperVisor().equalsIgnoreCase("lxc")) {
            virtService = "lxc:///";
        } else {
            virtService = "some xen";
        }

        try {
            Domain dom = null;
            synchronized (NovaWorker.getInstance().getConnLock()) {
                dom = NovaWorker.getInstance().getConn(virtService, false)
                        .domainLookupByUUIDString(msg.getUuid());
                // NovaWorker.getInstance().closeConnectToKvm();
            }
            if (dom == null) {
                VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
                        Vnode.Status.CONNECT_FAILURE);
                log.error("cannot connect and close domain " + msg.getUuid());
                return;
            }

            if (!msg.isSuspendOnly()) {
                VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
                        Vnode.Status.SHUTTING_DOWN);
                // if domain is running, shut it down
                if (dom.isActive() == 1) {
                    dom.destroy();
                    log.info("domain shut down");
                }
                if (msg.delvm && (dom.isPersistent() == 1)) {
                    dom.undefine();
                    log.info("domain undefined");
                } else {
                    VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
                            Vnode.Status.SHUT_OFF);
                    NovaWorker.getInstance().getVnodeIP()
                            .remove(UUID.fromString(msg.getUuid()));
                }

            } else {
                dom.suspend();
                log.info("domain suspended");
                VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
                        Vnode.Status.PAUSED);
            }

        } catch (LibvirtException ex) {
            log.error("error closing domain " + msg.getUuid(), ex);
        }
    }
}
