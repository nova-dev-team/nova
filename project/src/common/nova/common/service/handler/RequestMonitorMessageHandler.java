package nova.common.service.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.RequestMonitorMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RequestMonitorMessageHandler implements
		ISimpleHandler<RequestMonitorMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(RequestMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xfrom);
	}

}
