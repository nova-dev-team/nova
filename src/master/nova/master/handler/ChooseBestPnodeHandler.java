package nova.master.handler;

import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.ChooseBestPnodeMessage;
import nova.master.models.Pnode;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

//author: @Herb
public class ChooseBestPnodeHandler implements
        SimpleHandler<ChooseBestPnodeMessage> {

    public long pnodeid = -1;

    @Override
    public void handleMessage(ChooseBestPnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        pnodeid = -1;
        List<Pnode> allnodes = Pnode.all();
        int leastcount = 5000;
        for (Pnode node : allnodes) {
            if (node.getStatus() == Pnode.Status.RUNNING) {
                int pnodecount = node.getCurrentVMNum();
                if (node.getVmCapacity() > pnodecount
                        && pnodecount < leastcount) {
                    pnodeid = node.getId();
                    leastcount = pnodecount;
                }
            }

        }
    }

    public static void main(String args) {
        new ChooseBestPnodeHandler().handleMessage(null, null, null, null);
    }
}
