package nova.common.service.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.MonitorMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MonitorMessageHandler implements ISimpleHandler<MonitorMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(MonitorMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(msg.getMonitorInfo());
	}

}
