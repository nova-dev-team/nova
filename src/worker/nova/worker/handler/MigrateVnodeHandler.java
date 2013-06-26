package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.MasterProxy;
import nova.worker.NovaWorker;
import nova.worker.api.messages.MigrateVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

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
        // TODO @shayf finish migration
        Connect dconn = null;
        try {
            // Todo @shayf synchronized (NovaWorker.getInstance().connLock)
            // blabla
            // dconn = new Connect("qemu+ssh://username:passwd@ip:port/system",
            // true);
            // add by eagle
            MasterProxy master = NovaWorker.getInstance().getMaster();

            dconn = new Connect("qemu+ssh://" + msg.migrateToAddr.getIp()
                    + "/system");
            // eagle--end

            // TODO @shayf
            Connect sconn = NovaWorker.getInstance().getConn("qemu:///system",
                    false);
            Domain srcDomain = sconn.domainLookupByUUIDString(msg.vnodeUuid);
            long flag = 0;
            String uri = null;
            Domain dstDomain = srcDomain.migrate(dconn, flag,
                    srcDomain.getName(), uri, bandWidth);
            String strXML = dstDomain.getXMLDesc(0);
            int vncpos = strXML.indexOf("graphics type='vnc' port='");
            String strPort = strXML.substring(vncpos + 26, vncpos + 30);
            if (master != null) {
                master.sendMigrateComplete(msg.vnodeUuid,
                        msg.migrateToAddr.getIp(), strPort);
            }
            System.out.println(strPort);
        } catch (LibvirtException e1) {
            log.error("migrate error, maybe caused by libvirt ", e1);
        }
    }
}
