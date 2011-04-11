package nova.master.handler;

import nova.common.service.ISimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ReportVnodeStatusHandler implements
		ISimpleHandler<ReportVnodeStatusHandler.Message> {

	public static class Message {
		// TODO @santa populate fields
	}

	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO Auto-generated method stub

	}

}
