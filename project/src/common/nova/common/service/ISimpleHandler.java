package nova.common.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Interface for message handlers.
 * 
 * @author santa
 * 
 * @param <T>
 *            Type of the message to be handled.
 */
public interface ISimpleHandler<T> {

	/**
	 * Handle a certain message.
	 * 
	 * @param msg
	 *            The message to be handled.
	 * @param ctx
	 *            The {@link ChannelHandlerContext}.
	 * @param e
	 *            The {@link MessageEvent}.
	 * @param xfrom
	 *            Where is the message sent from.
	 */
	public void handleMessage(T msg, ChannelHandlerContext ctx, MessageEvent e,
			String xfrom);
}
