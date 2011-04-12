package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.master.NovaMaster;
import nova.master.api.messages.PnodeStatusMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class PnodeStatusMessageHandler implements
		ISimpleHandler<PnodeStatusMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(PnodeStatusMessageHandler.class);

	@Override
	public void handleMessage(PnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {

		// TODO @santa More verbose logging.
		log.info("update pnode status");
		NovaMaster.getInstance().getDB()
				.updatePnodeStatus(msg.pIdent, msg.status);

	}

}
