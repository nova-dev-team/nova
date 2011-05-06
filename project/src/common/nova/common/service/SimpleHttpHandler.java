package nova.common.service;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Simple http request handling routine.
 * 
 * @author santa
 * 
 */
public abstract class SimpleHttpHandler implements
		SimpleHandler<DefaultHttpRequest> {

	/**
	 * Handle an http request, send rendered page results.
	 */
	@Override
	public void handleMessage(DefaultHttpRequest req,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

		String replyText = renderResult(req);
		HttpResponse rep = new DefaultHttpResponse(req.getProtocolVersion(),
				HttpResponseStatus.OK);
		HttpHeaders.setContentLength(rep, replyText.length());

		ChannelBuffer buffer = HeapChannelBufferFactory.getInstance()
				.getBuffer(replyText.length());
		buffer.writeBytes(replyText.getBytes());
		rep.setContent(buffer);

		ChannelFuture writeFuture = e.getChannel().write(rep);

		if (!HttpHeaders.isKeepAlive(req)) {
			// Close the connection when the whole content is written out.
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}

	}

	/**
	 * Render response into a page.
	 * 
	 * @param req
	 *            The HTTP request.
	 * @return The rendered result page.
	 */
	abstract public String renderResult(DefaultHttpRequest req);

}
