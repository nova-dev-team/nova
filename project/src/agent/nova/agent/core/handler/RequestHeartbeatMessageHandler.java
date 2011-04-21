package nova.agent.core.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.api.AgentProxy;
import nova.agent.common.util.GlobalPara;
import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.RequestHeartbeatMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handle request heartbeat message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RequestHeartbeatMessageHandler implements
		ISimpleHandler<RequestHeartbeatMessage> {

	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger
			.getLogger(RequestHeartbeatMessageHandler.class);

	@Override
	public void handleMessage(RequestHeartbeatMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		/**
		 * Wake up AgentProxy when a server or a master discover this virtual
		 * machine
		 */

		if (!GlobalPara.agentProxyMap.containsKey(xreply)) {
			try {
				AgentProxy agentProxy = new AgentProxy(new InetSocketAddress(
						InetAddress.getLocalHost().getHostAddress(),
						GlobalPara.BIND_PORT));

				agentProxy.connect(xreply.getInetSocketAddress());
				agentProxy.sendHeartbeat();

				logger.info("General monitor proxy have connected to server "
						+ xreply);

				GlobalPara.agentProxyMap.put(xreply, agentProxy);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			/**
			 * use established channel to send GeneralMonitorMessage
			 */
		} else {
			AgentProxy agentProxy = GlobalPara.agentProxyMap.get(xreply);
			agentProxy.sendHeartbeat();
		}
	}
}
