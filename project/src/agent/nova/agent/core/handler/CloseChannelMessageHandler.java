package nova.agent.core.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.CloseChannelMessage;

import org.apache.log4j.Logger;
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
	static Logger logger = Logger.getLogger(CloseChannelMessageHandler.class);

	@Override
	public void handleMessage(CloseChannelMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		logger.info(xreply + " client channel closed");
	}
}