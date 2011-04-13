package nova.agent.core.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.SoftwareInstallStatusMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Report to master or worker the status of softwares' installation.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class SoftwareInstallStatusMessageHandler implements
		ISimpleHandler<SoftwareInstallStatusMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(SoftwareInstallStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xreply);
	}

}
