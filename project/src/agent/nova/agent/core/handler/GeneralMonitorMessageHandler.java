package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.GeneralMonitorMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class GeneralMonitorMessageHandler implements
		ISimpleHandler<GeneralMonitorMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(GeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(msg.getGeneralMonitorInfo());
	}
}
