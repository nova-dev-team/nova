package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.HeartbeatMessage;
import nova.master.NovaMaster;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterHeartbeatHandler implements ISimpleHandler<HeartbeatMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(MasterHeartbeatHandler.class);

	@Override
	public void handleMessage(HeartbeatMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {

		if (xreply == null) {
			log.warn("Got a heartbeat message, but the reply address is null!");
		} else {
			log.info("Got heartbeat message from: " + xreply);
		}

		// TODO @santa possibly update vnode
		NovaMaster.getInstance().getDB().updatePnodeAliveTime(xreply);

	}

}