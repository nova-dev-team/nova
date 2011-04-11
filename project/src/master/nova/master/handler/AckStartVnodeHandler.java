package nova.master.handler;

import nova.common.service.ISimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class AckStartVnodeHandler implements
		ISimpleHandler<AckStartVnodeHandler.Message> {

	public class Message {
		// TODO @santa populate the fields
	}

	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO @santa Auto-generated method stub

	}

}
