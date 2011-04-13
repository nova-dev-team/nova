package nova.agent.core.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.HeartbeatProxy;
import nova.agent.core.service.IntimeProxy;
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
		// Wake up heartbeatProxy when a master or a server discover this
		// virtual machine
		if (!GlobalPara.heartbeatProxyMap.containsKey(xreply)) {
			try {
				HeartbeatProxy heartbeatProxy = new HeartbeatProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.BIND_PORT));
				heartbeatProxy.connect(xreply.getInetSocketAddress());
				heartbeatProxy.sendHeartbeatMessage();

				logger.info("General heartbeat proxy have connected to server "
						+ xreply);
				// General heartbeat proxy can work
				synchronized (GlobalPara.heartbeatSem) {
					GlobalPara.heartbeatSem.notifyAll();
				}

				GlobalPara.heartbeatProxyMap.put(xreply, heartbeatProxy);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			// Wake up intimeProxy when there are additional
			// RequestHeartbeatMessages
		} else if (!GlobalPara.intimeProxyMap.containsKey(xreply)) {
			try {
				IntimeProxy intimeProxy = new IntimeProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.BIND_PORT));

				intimeProxy.connect(xreply.getInetSocketAddress());
				intimeProxy.sendHeartbeatMessage();

				logger.info("Intime proxy have connected to server " + xreply);

				GlobalPara.intimeProxyMap.put(xreply, intimeProxy);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			// use established channel to send HeartbeatMessage
		} else {
			IntimeProxy ip = (IntimeProxy) GlobalPara.intimeProxyMap
					.get(xreply);
			ip.sendHeartbeatMessage();
		}
	}
}
