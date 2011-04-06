package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.HeartbeatMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class HeartbeatMessageHandler implements
		ISimpleHandler<HeartbeatMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(HeartbeatMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xfrom);
	}
}
