package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.InstallApplianceMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * handler to prepare softwares
 * 
 * @author syf
 * 
 */
public class InstallApplianceHandler implements
		SimpleHandler<InstallApplianceMessage> {

	@Override
	public void handleMessage(InstallApplianceMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO @shayf download softwares and pack an iso file

	}

}
