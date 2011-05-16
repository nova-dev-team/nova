package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.CreateVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CreateVnodeHandler implements SimpleHandler<CreateVnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(CreateVnodeMessage.class);

	@Override
	public void handleMessage(CreateVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
