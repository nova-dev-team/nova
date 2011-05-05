package nova.worker.handler;

import java.util.HashMap;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.Utils;

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
public class WorkerHttpHandler extends SimpleHttpHandler {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(WorkerHttpHandler.class);

	/**
	 * Write a log.
	 */
	@Override
	public void handleMessage(DefaultHttpRequest req,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		log.info("New HTTP request from " + e.getChannel().getRemoteAddress());
		super.handleMessage(req, ctx, e, xreply);
	}

	/**
	 * Render result pages.
	 */
	@Override
	public String renderResult(DefaultHttpRequest req) {
		// TODO @shayf render results

		String fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "worker",
				"index.html");
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("content", "hello, this is nova worker!");
		return Utils.expandTemplateFile(fpath, values);
	}
}
