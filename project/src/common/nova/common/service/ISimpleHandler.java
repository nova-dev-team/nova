package nova.common.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public interface ISimpleHandler<T> {

	public void handleMessage(T msg, ChannelHandlerContext ctx, MessageEvent e,
			String xfrom);
}
