package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeleteVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeleteVnodeHandler implements SimpleHandler<DeleteVnodeMessage> {

	/**
	 * Log4j logger;
	 */
	Logger log = Logger.getLogger(DeleteVnodeMessage.class);

	@Override
	public void handleMessage(DeleteVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
