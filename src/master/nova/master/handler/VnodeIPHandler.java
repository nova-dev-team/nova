package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.VnodeIPMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class VnodeIPHandler implements SimpleHandler<VnodeIPMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(VnodeIPHandler.class);

    @Override
    public void handleMessage(VnodeIPMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {

        Vnode vnode = Vnode.findByUuid(msg.uuid);
        // cannot find the vnode uuid in database
        if (vnode == null) {
            // return
            log.info("it is not possible");
            return;
        }
        // update vnode status
        // vnode.setStatus(msg.status);
        // update vnode ip address
        log.info("Obtain real ip： " + msg.vnodeIp);
        if (msg.vnodeIp != null) {
            vnode.setIp(msg.vnodeIp);
        }
        vnode.save();

    }
}
