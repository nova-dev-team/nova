package nova.master.handler;

import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeletePnodeMessage;
import nova.master.api.messages.DeleteVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeletePnodeHandler implements SimpleHandler<DeletePnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(DeletePnodeMessage.class);

    @Override
    public void handleMessage(DeletePnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        System.out.println("~~~~~~~~~~~~~~~1~~~~~~~~~~~~~~~~~~");
        Pnode pnode = Pnode.findById(msg.id);
        if (pnode != null) {
            List<Vnode> all = Vnode.all();
            for (Vnode vnode : all) {
                if (vnode.getPmachineId() == msg.id) {
                    new DeleteVnodeHandler().handleMessage(
                            new DeleteVnodeMessage(vnode.getId()), null, null,
                            null);
                }
            }
            System.out.println("~~~~~~~~~~~~~~~2~~~~~~~~~~~~~~~~~~");
            log.info("Delete pnode: " + pnode.getHostname());
            Pnode.delete(pnode);
        } else {
            log.info("Pnode @ id: " + String.valueOf(msg.id) + "not exist.");
        }
    }
}
