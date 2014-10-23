package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.models.Pnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.QueryPnodeInfoMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class WorkerQueryPnodeInfoMessageHandler implements
        SimpleHandler<QueryPnodeInfoMessage> {

    @Override
    public void handleMessage(QueryPnodeInfoMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        NovaWorker.masteraddr = xreply;
        if (NovaWorker.getInstance().getMaster() == null
                || NovaWorker.getInstance().getMaster().isConnected() == false) {
            NovaWorker.getInstance().registerMaster(xreply);
        }
        NovaWorker
                .getInstance()
                .getMaster()
                .sendPnodeStatus(NovaWorker.getInstance().getAddr(),
                        Pnode.Status.RUNNING);

    }

}
