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
		Pair<String, String>[] appsFromMaster = msg.getApps();

		ConcurrentHashMap<String, Appliance> appliances = NovaAgent
				.getInstance().getAppliances();

		// Delete appliance that stay in local appliances but not in new master
		// appliance list
		for (Appliance app : appliances.values()) {
			for (Pair<String, String> appsMaster : appsFromMaster)
				if (appsMaster.getFirst().equals(app.getName()))
					appliances.remove(app.getName());
		}

		// Update to new list
		for (int i = 0; i < appsFromMaster.length; i++) {
			String appName = appsFromMaster[i].getFirst();

			if (appliances.containsKey(appName) == false) {
				Appliance app = new Appliance(appName);
				appliances.put(appName, app);
			}
			Appliance app = appliances.get(appName);
			app.setInfo(appsFromMaster[i].getSecond());
		}

		NovaAgent.getInstance().saveAppliances();
	}
}
