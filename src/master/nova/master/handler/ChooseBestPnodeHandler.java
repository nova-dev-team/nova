package nova.master.handler;

import java.util.HashMap;
import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.ChooseBestPnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ChooseBestPnodeHandler implements
        SimpleHandler<ChooseBestPnodeMessage> {

    public long pnodeid = -1;

    @Override
    public void handleMessage(ChooseBestPnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        pnodeid = -1;
        List<Pnode> allnodes = Pnode.all();
        List<Vnode> allvms = Vnode.all();
        HashMap<Integer, Integer> countmap = new HashMap<Integer, Integer>();
        for (Vnode vm : allvms) {
            Object obj = countmap.get(vm.getPmachineId());
            if (obj != null)
                countmap.put(vm.getPmachineId(),
                        countmap.get(vm.getPmachineId()) + 1);
            else
                countmap.put(vm.getPmachineId(), 1);
        }
        int leastcount = 0;
        for (Pnode node : allnodes) {
            if (node.isHeartbeatTimeout() == false) {
                int pnodecount = countmap.get(node.getId());
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
