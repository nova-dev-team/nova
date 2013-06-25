package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
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
    private long bandWidth = 1;

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
            dconn = new Connect("qemu+ssh://root@" + msg.migrateToAddr.getIp()
                    + "/system");
            // eagle--end

            // TODO @shayf
            Connect sconn = NovaWorker.getInstance().getConn("qemu:///system",
                    false);
            Domain srcDomain = sconn.domainLookupByUUIDString(msg.vnodeUuid);
            long flag = 0;
            String uri = null;
            srcDomain.migrate(dconn, flag, srcDomain.getName(), uri, bandWidth);
        } catch (LibvirtException e1) {
            log.error("migrate error, maybe caused by libvirt ", e1);
        }
    }
}
