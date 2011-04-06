package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class SoftwareInstallStatusMessageHandler implements
		ISimpleHandler<SoftwareInstallStatusMessageHandler> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(SoftwareInstallStatusMessageHandler msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xfrom);
	}

}
