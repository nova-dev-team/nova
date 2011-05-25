package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeleteVclusterMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeleteVclusterHandler implements
		SimpleHandler<DeleteVclusterMessage> {

	/**
	 * Log4j logger;
	 */
	Logger log = Logger.getLogger(DeleteVclusterHandler.class);

	@Override
	public void handleMessage(DeleteVclusterMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

	}

}
