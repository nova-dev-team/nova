package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.MigrateVnodeMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MigrateVnodeHandler implements SimpleHandler<MigrateVnodeMessage> {

	@Override
	public void handleMessage(MigrateVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO @shayf finish migrate process

	}

}
