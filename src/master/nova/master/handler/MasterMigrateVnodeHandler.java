package nova.master.handler;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

public class MasterMigrateVnodeHandler
        implements SimpleHandler<MasterMigrateVnodeMessage> {

    /**
     * Log4j logger;
     */
    Logger log = Logger.getLogger(MasterMigrateVnodeMessage.class);

    @Override
    public void handleMessage(MasterMigrateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        Vnode vnode = Vnode.findById(msg.vnodeId);
        Pnode pnodeFrom = Pnode.findById(msg.migrateFrom);
        Pnode pnodeTo = Pnode.findById(msg.migrateTo);

        vnode.setStatus(Vnode.Status.MIGRATING);
        vnode.save();

        WorkerProxy wp = new WorkerProxy(
                new SimpleAddress(Conf.getString("master.bind_host"),
                        Conf.getInteger("master.bind_port")));

        wp.connect(
                new InetSocketAddress(pnodeFrom.getIp(), pnodeFrom.getPort()));

        // send migrate request to worker
        // added the hypervisor type and the ip addr by Tianyu
        wp.sendMigrateVnode(vnode.getName(), vnode.getUuid(), pnodeTo.getAddr(),
                vnode.getHypervisor(), vnode.getIp());
    }
}
