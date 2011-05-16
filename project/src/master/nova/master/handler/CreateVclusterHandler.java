package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.CreateVclusterMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CreateVclusterHandler implements
		SimpleHandler<CreateVclusterMessage> {
	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(CreateVclusterMessage.class);

	@Override
	public void handleMessage(CreateVclusterMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
