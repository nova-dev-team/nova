package nova.master.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Vnode;

public class VnodeStatusHandler implements SimpleHandler<VnodeStatusMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(VnodeStatusHandler.class);

    @Override
    public void handleMessage(VnodeStatusMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {

        Vnode vnode = Vnode.findByUuid(msg.uuid);
        // cannot find the vnode uuid in database
        if (vnode == null) {
            // return
            return;
        }
        // update vnode status
        vnode.setStatus(msg.status);
        // update vnode ip address
        if (msg.vnodeIp != null) {
            vnode.setIp(msg.vnodeIp);
        }
        vnode.save();

    }
}
