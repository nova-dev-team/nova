package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.StartVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class StartVnodeHandler implements SimpleHandler<StartVnodeMessage> {

    @Override
    public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Vnode vnode = Vnode.findByUuid(msg.uuid);
        if (vnode == null)
            return;
        WorkerProxy wp = new WorkerProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));

        Pnode pnode = Pnode.findById(vnode.getPmachineId());
        wp.connect(new InetSocketAddress(pnode.getIp(), Conf
                .getInteger("worker.bind_port")));

        wp.sendStartExistVnode(vnode.getHypervisor(), vnode.getUuid(),
                vnode.getId());
    }
}
