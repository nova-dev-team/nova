package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.HeartbeatMessage;
import nova.master.models.Pnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterHeartbeatHandler implements SimpleHandler<HeartbeatMessage> {

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
		Pnode pnode = Pnode.findByIp(xreply.ip);
		if (pnode != null) {
			pnode.setStatus(Pnode.Status.RUNNING);
			pnode.save();
		} else {
			log.error("Pnode with host " + xreply.ip + " not found!");
		}

	}

}
