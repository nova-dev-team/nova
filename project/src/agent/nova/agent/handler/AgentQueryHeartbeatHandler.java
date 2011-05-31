package nova.agent.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.agent.NovaAgent;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handle request heartbeat message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentQueryHeartbeatHandler implements
		SimpleHandler<QueryHeartbeatMessage> {

	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger.getLogger(AgentQueryHeartbeatHandler.class);

	@Override
	public void handleMessage(QueryHeartbeatMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		MasterProxy proxy = NovaAgent.getInstance().getMaster();
		if (proxy == null) {
			NovaAgent.getInstance().registerMaster(xreply);
			proxy = NovaAgent.getInstance().getMaster();
		}
		if (proxy != null) {
			proxy.sendAgentHeartbeat();
		}

	}
}
