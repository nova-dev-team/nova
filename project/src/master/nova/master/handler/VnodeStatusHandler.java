package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class VnodeStatusHandler implements SimpleHandler<VnodeStatusMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VnodeStatusHandler.class);

	@Override
	public void handleMessage(VnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		// @zhaoxun Save update into database
		Vnode vnode = Vnode.findByIp(xreply.ip);
		if (vnode == null) {
			vnode = new Vnode();
			vnode.setAddr(xreply);
		}
		vnode.setStatus(msg.status);
		log.info("Update status of pnode @ " + vnode.getAddr() + " to "
				+ vnode.getStatus());
		vnode.save();

	}
}
