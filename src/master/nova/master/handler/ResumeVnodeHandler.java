package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.ResumeVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ResumeVnodeHandler implements SimpleHandler<ResumeVnodeMessage> {

    @Override
    public void handleMessage(ResumeVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Pnode pnode = Pnode.findById(msg.pnodeid);
        Vnode vnode = Vnode.findById(msg.vnodeid);

        WorkerProxy wp = new WorkerProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));

        wp.connect(new InetSocketAddress(pnode.getIp(), Conf
                .getInteger("worker.bind_port")));
        wp.sendWakeupVnode(msg.hypervisor, false, vnode.getUuid());
    }
}
