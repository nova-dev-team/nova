package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpRequestHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

/**
 * Master's handler for http requests.
 * 
 * @author santa
 * 
 */
public class MasterHttpRequestHandler extends SimpleHttpRequestHandler {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(MasterHttpRequestHandler.class);

	/**
	 * Handle a request message, render result pages.
	 */
	@Override
	public void handleMessage(DefaultHttpRequest req,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		log.info("New HTTP request from " + e.getChannel().getRemoteAddress());
		super.handleMessage(req, ctx, e, xreply);
	}

	/**
	 * Render the result pages.
	 */
	@Override
	public String renderResult(DefaultHttpRequest req) {
		// TODO @zhaoxun render results
		return "This shall be done!";
	}
}
