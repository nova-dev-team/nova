package nova.agent.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.GlobalPara;
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
	static Logger logger = Logger
			.getLogger(AgentQueryHeartbeatHandler.class);

	@Override
	public void handleMessage(QueryHeartbeatMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		/*
		 * Wake up AgentProxy when a server or a master discover this virtual
		 * machine
		 */

		if (!GlobalPara.masterProxyMap.containsKey(xreply)) {
			try {
				MasterProxy heartbeatProxy = new MasterProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.AGENT_BIND_PORT));

				heartbeatProxy.connect(xreply.getInetSocketAddress());
				heartbeatProxy.sendHeartbeat();

				logger.info("General monitor proxy have connected to server "
						+ xreply);

				GlobalPara.masterProxyMap.put(xreply, heartbeatProxy);
			} catch (UnknownHostException ex) {
				logger.error("Can't connect to host " + xreply, ex);
			}
			/*
			 * use established channel to send GeneralMonitorMessage
			 */
		} else {
			MasterProxy heartbeatProxy = GlobalPara.masterProxyMap.get(xreply);
			heartbeatProxy.sendHeartbeat();
		}
	}
}
