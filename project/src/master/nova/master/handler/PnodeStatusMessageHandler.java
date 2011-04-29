package nova.master.handler;

import nova.common.service.SimpleHandler;
import nova.common.service.SimpleAddress;
import nova.master.NovaMaster;
import nova.master.api.messages.PnodeStatusMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class PnodeStatusMessageHandler implements
		SimpleHandler<PnodeStatusMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(PnodeStatusMessageHandler.class);

	@Override
	public void handleMessage(PnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		// TODO @zhaoxun More verbose logging.
		log.info("update pnode status");
		NovaMaster.getInstance().getDB()
				.updatePnodeStatus(msg.pAddr, msg.status);

	}

}
