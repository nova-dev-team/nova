package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.QueryPnodeInfoMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class WorkerQueryPnodeInfoMessageHandler implements
		SimpleHandler<QueryPnodeInfoMessage> {

	@Override
	public void handleMessage(QueryPnodeInfoMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
