package nova.worker.handler;

import nova.common.service.ISimpleHandler;
import nova.worker.message.StartVnodeMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class StartVnodeHandler implements ISimpleHandler<StartVnodeMessage> {

	@Override
	public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO @santa Auto-generated method stub

	}

}
