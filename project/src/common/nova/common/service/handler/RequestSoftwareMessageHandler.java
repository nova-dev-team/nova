package nova.common.service.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RequestSoftwareMessageHandler implements
		ISimpleHandler<RequestSoftwareMessageHandler> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(RequestSoftwareMessageHandler msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xfrom);
	}

}
