package nova.agent.core.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.common.util.GlobalPara;
import nova.common.service.SimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.RequestGeneralMonitorMessage;
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
public class RequestGeneralMonitorMessageHandler implements
		SimpleHandler<RequestGeneralMonitorMessage> {
	AtomicLong counter = new AtomicLong();
	static Logger logger = Logger
			.getLogger(RequestGeneralMonitorMessageHandler.class);

	@Override
	public void handleMessage(RequestGeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		/**
		 * Wake up AgentProxy when a server or a master discover this virtual
		 * machine
		 */

		if (!GlobalPara.masterProxyMap.containsKey(xreply)) {
			try {
				MasterProxy monitorProxy = new MasterProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.BIND_PORT));

				monitorProxy.connect(xreply.getInetSocketAddress());
				monitorProxy.sendMonitorInfo();

				logger.info("General monitor proxy have connected to server "
						+ xreply);

				GlobalPara.masterProxyMap.put(xreply, monitorProxy);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				logger.error("Can't connect to host " + xreply);
			}
			/**
			 * use established channel to send GeneralMonitorMessage
			 */
		} else {
			MasterProxy monitorProxy = GlobalPara.masterProxyMap.get(xreply);
			monitorProxy.sendMonitorInfo();
		}
	}
}
