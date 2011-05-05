package nova.agent.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.agent.api.messages.InstallApplianceMessage;
import nova.common.service.SimpleHandler;
import nova.common.service.SimpleAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Report to master or worker the status of softwares' installation.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class SoftwareInstallStatusMessageHandler implements
		SimpleHandler<InstallApplianceMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(InstallApplianceMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		System.out.println(counter.incrementAndGet());
		System.out.println(msg.getClass().getName());
		System.out.println(xreply);
	}

}
