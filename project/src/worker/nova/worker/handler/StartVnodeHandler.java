package nova.worker.handler;

import nova.common.service.ISimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class StartVnodeHandler implements
		ISimpleHandler<StartVnodeHandler.Message> {

	public class Message {
		// TODO @santa pupulate fields
	}

	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO @santa Auto-generated method stub

	}

}
