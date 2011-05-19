package nova.master.handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.Utils;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.models.Pnode;

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

		/*
		 * MasterProxy mp = new MasterProxy(new SimpleAddress(
		 * Conf.getString("master.bind_host"), Integer.parseInt(Conf
		 * .getString("master.bind_port")))); mp.connect(new
		 * SimpleAddress(Conf.getString("master.bind_host"),
		 * Integer.parseInt(Conf.getString("master.bind_port")))
		 * .getInetSocketAddress()); System.out.println(new SimpleAddress(
		 * Conf.getString("master.bind_host"), Integer.parseInt(Conf
		 * .getString("master.bind_port"))).toString());
		 */

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

			System.out.println(act);
			if (act.equals("add_pnode")) {
				// System.out.println(111);
				new AddPnodeHandler().handleMessage(new AddPnodeMessage(
						new SimpleAddress(queryMap.get("pnode_ip"), 4000)),
						null, null, null);
				// System.out.println(222);
				// values.put("pnode_ip", "ip:" + queryMap.get("pnode_ip"));
			} else if (act.equals("register_vdisk1")) {
				new RegisterVdiskHandler().handleMessage(
						new RegisterVdiskMessage(queryMap.get("display_name"),
								queryMap.get("file_name"), queryMap
										.get("image_type"), queryMap
										.get("os_family"), queryMap
										.get("os_name"), queryMap
										.get("description")), null, null, null);
			} else if (act.equals("register_appliance1")) {
				new RegisterApplianceHandler().handleMessage(
						new RegisterApplianceMessage(queryMap
								.get("display_name"),
								queryMap.get("file_name"), queryMap
										.get("os_family"), queryMap
										.get("description")), null, null, null);
			} else if (act.equals("create_vnode1")) {
				new CreateVnodeHandler().handleMessage(
						new CreateVnodeMessage(queryMap.get("vm_image"),
								queryMap.get("vm_name"), Integer
										.parseInt(queryMap.get("cpu_count")),
								Integer.parseInt(queryMap.get("memory_size")),
								queryMap.get("appliance_list")), null, null,
						null);
			} else if (act.equals("create_vcluster")) {
				String vclusterFrame = "";
				for (int i = 0; i < Integer.parseInt(queryMap
						.get("vcluster_size")); i++) {
					vclusterFrame += "<h3><font color=\"#FF0000\">"
							+ "Create vnode"
							+ (i + 1)
							+ "<br></font></h3>"
							+ "<form action=\"create_vnode\" method=\"get\">"
							+ "VM Image<input type=\"text\" name=\"vnode_image\"><br>"
							+ "VM Name<input type=\"text\" name=\"vnode_name\"><br>"
							+ "CPU Count	<select name=\"cpu_count\" >"
							+ "<option value=\"1\">1"
							+ "<option value=\"2\">2"
							+ "<option value=\"4\">4"
							+ "</select>"
							+ "Memory Size<input type=\"text\" name=\"memory_size\">MB<br>"
							+ "Appliance<input type=\"text\" name=\"appliance\">"
							+ "<input type=\"submit\" value=\"Create vnode\">"
							+ "</form>";
				}
				values.put("vcluster_frame", vclusterFrame);
				/*
				 * new CreateVclusterHandler() .handleMessage( new
				 * CreateVclusterMessage(queryMap .get("vcluster_name"),
				 * Integer.parseInt(queryMap .get("vcluster_size"))), null,
				 * null, null);
				 */
			}
		}
		values.put("pnode_info", Pnode.all());

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
