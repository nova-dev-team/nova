package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.PnodeHeartbeatMessage;
import nova.master.models.Pnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterPnodeHeartbeatHandler implements
        SimpleHandler<PnodeHeartbeatMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(MasterPnodeHeartbeatHandler.class);

    @Override
    public void handleMessage(PnodeHeartbeatMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // System.out.println("Pnode Heartbeat");
        if (xreply == null) {
            log.warn("Got a pnode heartbeat message, but the reply address is null!");
        } else {
            // log.info("Got pnode heartbeat message from: " + xreply);
        }

        // TODO @zhaoxun possibly update vnode

        Pnode pnode = Pnode.findByIp(xreply.ip);
        if (pnode != null) {
            pnode.setStatus(Pnode.Status.RUNNING);
            log.info("Update status of pnode @ " + pnode.getAddr() + " to "
                    + pnode.getStatus());
            pnode.updateLastAckTime();
            pnode.save();
        } else {
            log.error("Pnode with host " + xreply.ip + " not found!");
        }

    }
}
