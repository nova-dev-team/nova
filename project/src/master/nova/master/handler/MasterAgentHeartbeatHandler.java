package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterAgentHeartbeatHandler implements
		SimpleHandler<AgentHeartbeatMessage> {

	/**
	 * Log4j logger;
	 */
	Logger log = Logger.getLogger(MasterAgentHeartbeatHandler.class);

	@Override
	public void handleMessage(AgentHeartbeatMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		if (xreply == null) {
			log.warn("Got an agent heartbeat message, but the reply address is null!");
		} else {
			log.info("Got agent heartbeat message from: " + xreply);
		}

		// TODO @zhaoxun possibly update vnode.agentStatus
		Vnode vnode = Vnode.findByIp(xreply.ip);
		if (vnode != null) {
			vnode.setAgentStatus("on");
			log.info("Update status of agent @ " + vnode.getAddr() + " to "
					+ vnode.getAgentStatus());
			vnode.save();
		} else {
			log.error("Vnode with host " + xreply.ip + " not found!");
		}
	}

}
