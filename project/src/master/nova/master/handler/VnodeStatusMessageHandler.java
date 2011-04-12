package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.master.api.messages.VnodeStatusMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class VnodeStatusMessageHandler implements
		ISimpleHandler<VnodeStatusMessage> {

	@Override
	public void handleMessage(VnodeStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		// TODO @zhaoxun Save update into database

	}

}
