package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.StopVnodeMessage;
import nova.master.models.Pnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class StopVnodeHandler implements SimpleHandler<StopVnodeMessage> {

    @Override
    public void handleMessage(StopVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        /**
         * Log4j logger.
         */
        Logger log = Logger.getLogger(AddPnodeMessage.class);

        WorkerProxy wp = new WorkerProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));

        Pnode pnode = Pnode.findById(Integer.parseInt(msg.pnodid));
        wp.connect(new InetSocketAddress(pnode.getIp(), Conf
                .getInteger("worker.bind_port")));
        wp.sendStopVnode(msg.hyperVisor, msg.uuid, msg.suspendOnly, false);
    }
}
