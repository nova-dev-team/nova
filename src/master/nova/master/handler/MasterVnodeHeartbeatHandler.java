package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterVnodeHeartbeatHandler implements
        SimpleHandler<AgentHeartbeatMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(MasterVnodeHeartbeatHandler.class);

    @Override
    public void handleMessage(AgentHeartbeatMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        if (xreply == null) {
            log.warn("Got a vnode heartbeat message, but the reply address is null!");
        } else {
            // log.info("Got vnode heartbeat message from: " + xreply);
        }

        // TODO @zhaoxun possibly update vnode

        Vnode vnode = Vnode.findByIp(xreply.ip);
        if (vnode != null) {
            vnode.setStatus(Vnode.Status.RUNNING);
            // log.info("Update status of vnode @ " + vnode.getAddr() + " to "
            // + vnode.getStatus());
            vnode.save();
        } else {
            log.error("Vnode with host " + xreply.ip + " not found!");
        }

    }
}
