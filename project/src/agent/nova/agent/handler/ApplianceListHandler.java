package nova.agent.handler;

import java.util.concurrent.ConcurrentHashMap;

import nova.agent.NovaAgent;
import nova.agent.api.messages.ApplianceListMessage;
import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Pair;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Reassign appliances' information of agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceListHandler implements
		SimpleHandler<ApplianceListMessage> {

	@Override
	public void handleMessage(ApplianceListMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// Pair.first = appName;
		// Pair.second = appInfo;
		Pair<String, String>[] apps = msg.getApps();

		ConcurrentHashMap<String, Appliance> appliances = NovaAgent
				.getInstance().getAppliances();

		for (int i = 0; i < apps.length; i++) {
			String appName = apps[i].getFirst();

			if (appliances.containsKey(appName) == false) {
				Appliance app = new Appliance(appName);
				appliances.put(appName, app);
			}
			Appliance app = appliances.get(appName);
			app.setInfo(apps[i].getSecond());
		}

		NovaAgent.getInstance().saveAppliances();
	}
}
