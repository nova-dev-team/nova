package nova.agent.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.GlobalPara;
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
public class AgentQueryPerfHandler implements
		SimpleHandler<QueryPerfMessage> {
	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger
			.getLogger(AgentQueryPerfHandler.class);

	@Override
	public void handleMessage(QueryPerfMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		/*
		 * Wake up AgentProxy when a server or a master discover this virtual
		 * machine
		 */

		if (!GlobalPara.masterProxyMap.containsKey(xreply)) {
			try {
				MasterProxy monitorProxy = new MasterProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.AGENT_BIND_PORT));

				monitorProxy.connect(xreply.getInetSocketAddress());
				monitorProxy.sendMonitorInfo();

				logger.info("General monitor proxy have connected to server "
						+ xreply);

				GlobalPara.masterProxyMap.put(xreply, monitorProxy);
			} catch (UnknownHostException ex) {
				logger.error("Can't connect to host " + xreply, ex);
			}
			/*
			 * use established channel to send GeneralMonitorMessage
			 */
		} else {
			MasterProxy monitorProxy = GlobalPara.masterProxyMap.get(xreply);
			monitorProxy.sendMonitorInfo();
		}
	}
}
