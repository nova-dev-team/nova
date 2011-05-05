package nova.agent.handler;

import java.util.concurrent.atomic.AtomicLong;

import nova.agent.api.messages.InstallApplianceMessage;
import nova.agent.daemons.PackageInstallDaemon;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Report to master or worker the status of softwares' installation.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class InstallApplianceHandler implements
		SimpleHandler<InstallApplianceMessage> {
	AtomicLong counter = new AtomicLong();

	@Override
	public void handleMessage(InstallApplianceMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		PackageInstallDaemon.getInstance().markInstall(msg.getAppName());
	}

}
