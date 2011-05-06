package nova.agent.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.NovaAgent;
import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.MasterProxy;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Download and install softwares in receive soft list.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class QueryApplianceStatusHandler implements
		SimpleHandler<QueryApplianceStatusMessage> {
	public AtomicLong counter = new AtomicLong();

	public ExecutorService softDownloadPool = Executors.newFixedThreadPool(1);
	public ExecutorService softInstallPool = Executors.newFixedThreadPool(1);

	@Override
	public void handleMessage(QueryApplianceStatusMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress reply) {

		MasterProxy proxy = NovaAgent.getInstance().getMaster();
		if (proxy == null) {
			NovaAgent.getInstance().registerMaster(reply);
			proxy = NovaAgent.getInstance().getMaster();
		}
		if (proxy != null) {
			ConcurrentHashMap<String, Appliance> appliances = NovaAgent
					.getInstance().getAppliances();

			proxy.sendApplianceStatus(appliances.values());
		}

	}
}
