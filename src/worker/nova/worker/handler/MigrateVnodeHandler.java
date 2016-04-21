package nova.worker.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.NovaWorker;
import nova.worker.api.messages.MigrateVnodeMessage;

/**
 * worker side migration handler
 * 
 * @author Tianyu Chen
 *
 */
public class MigrateVnodeHandler implements SimpleHandler<MigrateVnodeMessage> {

    /**
     * band width during migration
     */
    private long bandWidth = 100;

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(MigrateVnodeHandler.class);

    @Override
    public void handleMessage(MigrateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        try {
            NovaWorker.masteraddr = xreply;
            if (NovaWorker.getInstance().getMaster() == null || NovaWorker
                    .getInstance().getMaster().isConnected() == false) {
                NovaWorker.getInstance().registerMaster(xreply);
            }

            // get connections, these operations are hypervisor dependent
            String duri, suri;
            if (msg.hypervisor.equalsIgnoreCase("kvm")) {
                duri = "qemu+ssh://" + msg.migrateToAddr.getIp() + "/system";
                suri = "qemu:///system";
            } else if (msg.hypervisor.equalsIgnoreCase("lxc")) {
                duri = "lxc+ssh://" + msg.migrateToAddr.getIp();
                suri = "lxc:///";
            } else {
                // the hypervisor is not supported
                log.error("unsupported hypervisor migration! ");
                return;
            }

            // connect to destination (remote) machine
            Connect dconn = new Connect(duri);
            if (!dconn.isConnected()) {
                log.error("connect to destination failed! migration aborted");
                return;
            }
            // connect to source (local) machine
            Connect sconn = NovaWorker.getInstance().getConn(suri, false);
            if (!sconn.isConnected()) {
                log.error("connect to source failed! migration aborted");
                return;
            }

            // get source domain
            Domain srcDomain = sconn.domainLookupByUUIDString(msg.vnodeUuid);
            log.info("uuid of domain to migrate is " + msg.vnodeUuid);

            if (msg.hypervisor.equalsIgnoreCase("kvm")) {
                long flag = 0;
                String uri = null;
                Domain dstDomain = srcDomain.migrate(dconn, flag,
                        srcDomain.getName(), uri, bandWidth);
                String strXML = dstDomain.getXMLDesc(0);
                int vncpos = strXML.indexOf("graphics type='vnc' port='");
                String strPort = strXML.substring(vncpos + 26, vncpos + 30);
                NovaWorker.getInstance().getMaster().sendMigrateComplete(
                        msg.vnodeUuid, msg.migrateToAddr.getIp(), strPort);
                log.info("port: " + strPort);
            } else if (msg.hypervisor.equalsIgnoreCase("lxc")) {
                // do migration here
                // get the xml definition of the src domain
                String xml = srcDomain.getXMLDesc(0);
                // if domain is active, shut it down first
                if (srcDomain.isActive() == 1) {
                    srcDomain.destroy();
                }
                // undefine the source domain
                srcDomain.undefine();
                Domain dstDomain = dconn.domainDefineXML(xml);
                dstDomain.create();
            } else {
                log.error("unsupported hypervisor migration! ");
                return;
            }
        } catch (LibvirtException e1) {
            log.error("migrate error, maybe caused by libvirt ", e1);
        }
    }
}
