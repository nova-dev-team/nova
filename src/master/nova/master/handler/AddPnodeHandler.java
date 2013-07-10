package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.models.Pnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class AddPnodeHandler implements SimpleHandler<AddPnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(AddPnodeMessage.class);

    @Override
    public void handleMessage(AddPnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        System.out.println("333");
        Pnode pnode = Pnode.findByIp(msg.pAddr.getIp());
        if (pnode == null) {
            // new pnode
            pnode = new Pnode();
            pnode.setAddr(msg.pAddr);
            pnode.setVmCapacity(msg.vmCapacity);
            pnode.setStatus(Pnode.Status.ADD_PENDING);
            pnode.save();
            log.info("Added new pnode: " + pnode.getAddr());
        } else {
            // pnode exists
            if (pnode.getStatus().equals(Pnode.Status.RUNNING)) {
                // already working
                log.info("Pnode @" + pnode.getAddr() + " already RUNNING");
            } else {
                pnode.setStatus(Pnode.Status.ADD_PENDING);
                pnode.save();
                log.info("Pnode @" + pnode.getAddr()
                        + " already added, set to ADD_PENDING");
            }

        }
    }
}
