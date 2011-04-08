package nova.agent.core.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.CloseChannelMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Close this channel.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class CloseChannelMessageHandler implements
		ISimpleHandler<CloseChannelMessage> {

	@Override
	public void handleMessage(CloseChannelMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		// System.out.println("Closed!");
	}
}
