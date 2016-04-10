package nova.worker.handler;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartExistVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;

public class StartExistVnodeHandler
        implements SimpleHandler<StartExistVnodeMessage> {
    Logger log = Logger.getLogger(StartVnodeHandler.class);

    @Override
    public void handleMessage(StartExistVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // get master instance
        NovaWorker.masteraddr = xreply;
        if (NovaWorker.getInstance().getMaster() == null || NovaWorker
                .getInstance().getMaster().isConnected() == false) {
            NovaWorker.getInstance().registerMaster(xreply);
        }

        // get virtual driver URI
        final String virtService;
        if (msg.hyper.equalsIgnoreCase("kvm")) {
            virtService = "qemu:///system";
        } else if (msg.hyper.equalsIgnoreCase("vstaros")) {
            virtService = "vstaros:///system";
        } else if (msg.hyper.equalsIgnoreCase("lxc")) {
            virtService = "lxc:///";
        } else {
            virtService = "some xen";
        }

        try {
            // for debug
            log.info("trying to restart domain with uuid " + msg.uuid);
            // connect to the domain with the uuid
            Domain dom = null;
            synchronized (NovaWorker.getInstance().getConnLock()) {
                dom = NovaWorker.getInstance().getConn(virtService, false)
                        .domainLookupByUUIDString(msg.uuid);
            }
            if (dom == null) {
                VnodeStatusDaemon.putStatus(UUID.fromString(msg.uuid),
                        Vnode.Status.CONNECT_FAILURE);
                log.error("failed to connect to domain " + msg.uuid);
                return;
            }

            // temporary hack for lxc container
            // for debug
            log.info("domain running status: " + dom.isActive());
            if (msg.hyper.equalsIgnoreCase("lxc")) {
                log.info("lxc restarting...");
                dom.create();
                // will not send extra message to master since vnc port is not
                // updated
                return;
            }

            // modify the domain xml and update the vnc port
            String xml = dom.getXMLDesc(0);
            dom.undefine();
            int vncport = Utils.getFreePort();
            String prexml = xml.substring(0, xml.indexOf("type='vnc' port="))
                    + "type='vnc' port='";
            String talxml = xml
                    .substring(xml.indexOf("type='vnc' port='") + 21);
            xml = prexml + vncport + talxml;
            // redefine domain
            Domain testDomain = NovaWorker.getInstance()
                    .getConn(virtService, false).domainDefineXML(xml);
            testDomain.create();
            VnodeStatusDaemon.putStatus(UUID.fromString(msg.uuid),
                    Vnode.Status.PREPARING);
            // remap vnc port
            Utils.WORKER_VNC_MAP.put(msg.uuid, String.valueOf(vncport));
            NovaWorker.getInstance().getMaster().sendPnodeCreateVnodeMessage(
                    NovaWorker.getInstance().getAddr().getIp(), msg.vnodeid,
                    vncport, msg.uuid, msg.hyper);

        } catch (LibvirtException ex) {
            log.error("Error starting domain " + msg.uuid, ex);
        }
    }

}
