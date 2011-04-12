package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.HeartbeatMessage;
import nova.master.NovaMaster;
import nova.master.models.Pnode;

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
			MessageEvent e, String xfrom) {
		// TODO @santa Auto-generated method stub

		log.info("Got heartbeat message from: " + xfrom);

		// TODO @santa cleanup this ugly code
		String[] splt = xfrom.split(":");
		if (splt.length < 2) {
			return;
		}

		Pnode.Identity pIdent = new Pnode.Identity(splt[0],
				Integer.parseInt(splt[1]));
		NovaMaster.getInstance().getDB().updatePnodeAliveTime(pIdent);

	}

}
