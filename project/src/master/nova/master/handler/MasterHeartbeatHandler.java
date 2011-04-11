package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.HeartbeatMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterHeartbeatHandler implements ISimpleHandler<HeartbeatMessage> {

	@Override
	public void handleMessage(HeartbeatMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO @santa Auto-generated method stub

	}

}
