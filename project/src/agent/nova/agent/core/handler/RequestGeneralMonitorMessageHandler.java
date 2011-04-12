package nova.agent.core.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.IntimeProxy;
import nova.common.service.ISimpleHandler;
import nova.common.service.message.RequestGeneralMonitorMessage;

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

	@Override
	public void handleMessage(RequestGeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		// Wake up GeneralMonitorProxy when a server or a master discover this
		// virtual machine
		if (!GlobalPara.generalMonitorProxyMap.containsKey(xfrom)) {
			try {
				GeneralMonitorProxy gmp = new GeneralMonitorProxy(
						new InetSocketAddress(InetAddress.getLocalHost()
								.getHostAddress(), GlobalPara.BIND_PORT));

				String address = xfrom.split(":")[0].toString().trim();
				int port = Integer.parseInt(xfrom.split(":")[1].toString()
						.trim());
				gmp.connect(new InetSocketAddress(address, port));
				gmp.sendGeneralMonitorMessage();
				synchronized (GlobalPara.generalMonitorSem) {
					GlobalPara.generalMonitorSem.notifyAll();
				}

				GlobalPara.generalMonitorProxyMap.put(xfrom, gmp);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			// Wake up intimeProxy when there are additional
			// RequestGeneralMonitorMessages
		} else if (!GlobalPara.intimeProxyMap.containsKey(xfrom)) {
			try {
				IntimeProxy ip = new IntimeProxy(new InetSocketAddress(
						InetAddress.getLocalHost().getHostAddress(),
						GlobalPara.BIND_PORT));

				String address = xfrom.split(":")[0].toString().trim();
				int port = Integer.parseInt(xfrom.split(":")[1].toString()
						.trim());
				ip.connect(new InetSocketAddress(address, port));
				ip.sendGeneralMonitorMessage();

				GlobalPara.intimeProxyMap.put(xfrom, ip);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			// use established channel to send GeneralMonitorMessage
		} else {
			IntimeProxy ip = (IntimeProxy) GlobalPara.intimeProxyMap.get(xfrom);
			ip.sendGeneralMonitorMessage();
		}
	}
}
