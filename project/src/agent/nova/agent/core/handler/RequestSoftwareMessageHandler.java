package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.RequestSoftwareMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Install softwares in receive soft list.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RequestSoftwareMessageHandler implements
		ISimpleHandler<RequestSoftwareMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(RequestSoftwareMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		// ArrayList<String> softList = msg.getInstallSoftList();
		// call download and install process
	}
}
