package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class VnodeStatusHandler implements SimpleHandler<VnodeStatusMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(VnodeStatusHandler.class);

    @Override
    public void handleMessage(VnodeStatusMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        // @zhaoxun Save update into database
        Vnode vnode = Vnode.findByIp(msg.vnodeIp);
        if (vnode == null) {
            return;
            /*
             * vnode = new Vnode(); vnode.setAddr(new SimpleAddress(msg.vnodeIp,
             * Conf .getInteger("worker.bind_port")));
             */
        }
        vnode.setUuid(msg.uuid);
        vnode.setStatus(msg.status);

        log.info("Update status of vnode @ " + vnode.getIp() + " to "
                + vnode.getStatus());
        vnode.save();

    }
}
