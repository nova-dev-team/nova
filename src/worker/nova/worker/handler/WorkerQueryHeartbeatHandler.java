package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.worker.NovaWorker;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class WorkerQueryHeartbeatHandler implements
        SimpleHandler<QueryHeartbeatMessage> {

    @Override
    public void handleMessage(QueryHeartbeatMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        NovaWorker.masteraddr = xreply;
        if (NovaWorker.getInstance().getMaster() == null
                || NovaWorker.getInstance().getMaster().isConnected() == false) {
            NovaWorker.getInstance().registerMaster(xreply);
        }
        NovaWorker.getInstance().getMaster().sendPnodeHeartbeat();
    }
}
