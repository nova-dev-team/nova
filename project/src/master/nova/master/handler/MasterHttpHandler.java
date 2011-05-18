package nova.master.handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;

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
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("content", "NOVA MASTER");
		// Pnode pnode = new Pnode();
		// pnode.setIp("0.0.0.0");

		MasterProxy mp = new MasterProxy(new SimpleAddress("10.0.1.240", 3000));

		URL url = null;
		try {
			url = new URL("http://10.0.1.240:3000" + req.getUri());
		} catch (MalformedURLException e) {
			log.error(e);
		}

		String act = getAction(url.getFile());
		values.put("act", "act:" + act);
		Map<String, String> queryMap = null;
		if (act != null) {
			String query = url.getQuery();
			queryMap = getQueryMap(query);

			if (act == "add_pnode1") {
				mp.sendAddPnode(new SimpleAddress(queryMap.get("pnode_ip"),
						3000));
			} else if (act == "register_vdisk1") {
				mp.SendRegisterVdisk(queryMap.get("display_name"),
						queryMap.get("file_name"), queryMap.get("image_type"),
						queryMap.get("os_family"), queryMap.get("os_name"),
						queryMap.get("description"));
			} else if (act == "register_appliance1") {
				mp.SendRegisterAppliance(queryMap.get("display_name"),
						queryMap.get("file_name"), queryMap.get("os_family"),
						queryMap.get("description"));
			} else if (act == "create_vnode1") {
				mp.sendCreateVnode(queryMap.get("vm_image"),
						queryMap.get("vm_name"),
						Integer.parseInt(queryMap.get("cpu_count")),
						Integer.parseInt(queryMap.get("memory_size")),
						queryMap.get("appliance_list"));
			} else if (act == "create_vcluster") {
				mp.sendCreateVcluster(queryMap.get("vcluster_name"),
						Integer.parseInt(queryMap.get("vcluster_size")));
			}

			// values.put("pnode_ip", "ip:" + queryMap.get("pnode_ip"));
		}

		return Utils.expandTemplateFile(fpath, values);

	}

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = null;
			if (param.split("=").length > 1) {
				value = param.split("=")[1];
			}
			map.put(name, value);
		}
		return map;
	}

	public static String getAction(String actFile) {
		if (actFile.length() > 1) {
			String[] act = actFile.split("\\?");
			return act[0].split("/")[1];
		} else
			return null;
	}
}
