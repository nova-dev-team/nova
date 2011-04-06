package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.RequestGeneralMonitorMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RequestGeneralMonitorMessageHandler implements
		ISimpleHandler<RequestGeneralMonitorMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(RequestGeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xfrom);
	}

}
