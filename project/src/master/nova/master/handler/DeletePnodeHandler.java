package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeletePnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeletePnodeHandler implements SimpleHandler<DeletePnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(DeletePnodeMessage.class);

	@Override
	public void handleMessage(DeletePnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
