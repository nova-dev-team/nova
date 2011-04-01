package nova.common.service.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.CloseChannelMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CloseChannelMessageHandler implements
		ISimpleHandler<CloseChannelMessage> {

	@Override
	public void handleMessage(CloseChannelMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println("Closed!");
	}
}
