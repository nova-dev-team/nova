package nova.agent.core.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.IntimeProxy;
import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.RequestGeneralMonitorMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handle request general monitor message
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RequestGeneralMonitorMessageHandler implements
		ISimpleHandler<RequestGeneralMonitorMessage> {
	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger
			.getLogger(RequestGeneralMonitorMessageHandler.class);

	@Override
	public void handleMessage(RequestGeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		/**
		 * Wake up GeneralMonitorProxy when a server or a master discover this
		 * virtual machine
		 */

		if (!GlobalPara.generalMonitorProxyMap.containsKey(xreply)) {
			try {
				GeneralMonitorProxy gmp = new GeneralMonitorProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.BIND_PORT));

				gmp.connect(xreply.getInetSocketAddress());
				gmp.sendGeneralMonitorMessage();

				logger.info("General monitor proxy have connected to server "
						+ xreply);

				// generalMonitorProxy can work
				if (GlobalPara.generalMonitorProxyMap.isEmpty()) {
					synchronized (GlobalPara.generalMonitorSem) {
						GlobalPara.generalMonitorSem.notifyAll();
					}
				}

				GlobalPara.generalMonitorProxyMap.put(xreply, gmp);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			// Wake up intimeProxy when there are additional
			// RequestGeneralMonitorMessages
		} else if (!GlobalPara.intimeProxyMap.containsKey(xreply)) {
			try {
				IntimeProxy ip = new IntimeProxy(new InetSocketAddress(
						InetAddress.getLocalHost().getHostAddress(),
						GlobalPara.BIND_PORT));

				ip.connect(xreply.getInetSocketAddress());
				ip.sendGeneralMonitorMessage();

				logger.info("Intime proxy have connected to server " + xreply);

				GlobalPara.intimeProxyMap.put(xreply, ip);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			// use established channel to send GeneralMonitorMessage
		} else {
			IntimeProxy ip = (IntimeProxy) GlobalPara.intimeProxyMap
					.get(xreply);
			ip.sendGeneralMonitorMessage();
		}
	}
}
