package nova.common.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import sun.net.ftp.FtpProtocolException;

/**
 * Interface for message handlers.
 * 
 * @author santa
 * 
 * @param <T>
 *            Type of the message to be handled.
 */
public interface SimpleHandler<T> {

    /**
     * Handle a certain message.
     * 
     * @param msg
     *            The message to be handled.
     * @param ctx
     *            The {@link ChannelHandlerContext}.
     * @param e
     *            The {@link MessageEvent}.
     * @param xreply
     *            Where you should reply the message to.
     * @throws FtpProtocolException
     */
    public void handleMessage(T msg, ChannelHandlerContext ctx, MessageEvent e,
            SimpleAddress xreply) throws FtpProtocolException;
}
