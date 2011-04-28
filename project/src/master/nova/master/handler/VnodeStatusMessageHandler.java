package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.master.api.messages.VnodeStatusMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class VnodeStatusMessageHandler implements
		ISimpleHandler<VnodeStatusMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VnodeStatusMessageHandler.class);

	@Override
	public void handleMessage(VnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO @zhaoxun Save update into database

		log.info("update vnode status");

	}

}
