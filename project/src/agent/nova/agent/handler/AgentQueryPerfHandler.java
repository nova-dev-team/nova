package nova.agent.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.agent.NovaAgent;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.QueryPerfMessage;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handle request general monitor message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentQueryPerfHandler implements SimpleHandler<QueryPerfMessage> {
	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger.getLogger(AgentQueryPerfHandler.class);

	@Override
	public void handleMessage(QueryPerfMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {

		MasterProxy proxy = NovaAgent.getInstance().getMaster();
		if (proxy == null) {
			NovaAgent.getInstance().registerMaster(xreply);
			proxy = NovaAgent.getInstance().getMaster();
		}
		if (proxy != null) {
			proxy.sendVnodeMonitorInfo();
		}

	}
}
