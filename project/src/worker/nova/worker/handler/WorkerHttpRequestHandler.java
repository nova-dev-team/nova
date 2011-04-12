package nova.worker.handler;

import nova.common.service.SimpleHttpRequestHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

/**
 * Worker's handler for http requests.
 * 
 * @author santa
 * 
 */
public class WorkerHttpRequestHandler extends SimpleHttpRequestHandler {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(WorkerHttpRequestHandler.class);

	/**
	 * Write a log.
	 */
	@Override
	public void handleMessage(DefaultHttpRequest req,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {
		log.info("New HTTP request from " + e.getChannel().getRemoteAddress());
		super.handleMessage(req, ctx, e, xfrom);
	}

	/**
	 * Render result pages.
	 */
	@Override
	public String renderResult(DefaultHttpRequest req) {
		// TODO @santa render results
		return "This shall be done!";
	}
}
