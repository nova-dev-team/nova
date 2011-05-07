package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.models.Pnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class PnodeStatusHandler implements SimpleHandler<PnodeStatusMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(PnodeStatusHandler.class);

	@Override
	public void handleMessage(PnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		// TODO @zhaoxun More verbose logging.
		log.info("update pnode status");
		Pnode pnode = Pnode.findByIp(xreply.ip);
		if (pnode != null) {
			pnode.setStatus(msg.status);
			pnode.save();
		} else {
			log.error("Pnode with host " + xreply.ip + " not found!");
		}

	}

}
