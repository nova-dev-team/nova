package nova.master.handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.Utils;

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
public class MasterHttpHandler extends SimpleHttpHandler {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(MasterHttpHandler.class);

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
		String fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
				"index.html");
		// Pnode pnode = new Pnode();
		// pnode.setIp("0.0.0.0");

		URL url = null;
		try {
			url = new URL("http://10.0.1.240:3000/" + req.getUri());
		} catch (MalformedURLException e) {
			log.error(e);
		}

		String act = getAction(url.getFile());
		String query = url.getQuery();
		Map<String, String> queryMap = getQueryMap(query);

		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("content", "NOVA MASTER");
		// values.put("pnode_info", urilfpath);
		return Utils.expandTemplateFile(fpath, values);

	}

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	public static String getAction(String actFile) {
		String[] act = actFile.split("\\?");
		return act[0].split("/")[1];
	}
}
