package nova.worker.handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartExistVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class StartExistVnodeHandler implements
        SimpleHandler<StartExistVnodeMessage> {
    Logger log = Logger.getLogger(StartVnodeHandler.class);

    public static int getFreePort() {
        ServerSocket s = null;
        int MINPORT = 5901;
        int MAXPORT = 6900;
        for (; MINPORT < MAXPORT; MINPORT++) {
            try {
                s = new ServerSocket(MINPORT);
                s.close();
                return MINPORT;
            } catch (IOException e) {
                continue;
            }
        }
        return -1;

    }

    @Override
    public void handleMessage(StartExistVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        NovaWorker.masteraddr = xreply;
        if (NovaWorker.getInstance().getMaster() == null
                || NovaWorker.getInstance().getMaster().isConnected() == false) {
            NovaWorker.getInstance().registerMaster(xreply);
        }
        final String virtService;
        if (msg.hyper.equalsIgnoreCase("kvm")) {
            virtService = "qemu:///system";
        } else if (msg.hyper.equalsIgnoreCase("vstaros")) {
            virtService = "vstaros:///system";
        } else {
            virtService = "some xen";
        }

        try {
            Domain dom = null;
            synchronized (NovaWorker.getInstance().getConnLock()) {
                dom = NovaWorker.getInstance().getConn(virtService, false)
                        .domainLookupByUUIDString(msg.uuid);
                // NovaWorker.getInstance().closeConnectToKvm();
            }
            if (dom == null) {
                VnodeStatusDaemon.putStatus(UUID.fromString(msg.uuid),
                        Vnode.Status.CONNECT_FAILURE);
                log.error("cannot connect and start domain " + msg.uuid);
                return;
            }
            String xml = dom.getXMLDesc(0);
            dom.undefine();
            int vncport = getFreePort();
            String prexml = xml.substring(0, xml.indexOf("type='vnc' port="))
                    + "type='vnc' port='";
            String talxml = xml
                    .substring(xml.indexOf("type='vnc' port='") + 21);
            xml = prexml + vncport + talxml;
            Domain testDomain = NovaWorker.getInstance()
                    .getConn(virtService, false).domainDefineXML(xml);
            testDomain.create();
            VnodeStatusDaemon.putStatus(UUID.fromString(msg.uuid),
                    Vnode.Status.PREPARING);

            Utils.WORKER_VNC_MAP.put(msg.uuid, String.valueOf(vncport));
            NovaWorker
                    .getInstance()
                    .getMaster()
                    .sendPnodeCreateVnodeMessage(
                            NovaWorker.getInstance().getAddr().getIp(),
                            msg.vnodeid, vncport, msg.uuid);

        } catch (LibvirtException ex) {
            log.error("Error starting domain " + msg.uuid, ex);
        }
    }

}
