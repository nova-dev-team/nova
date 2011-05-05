package nova.agent.handler;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.GlobalPara;
import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.common.service.SimpleHandler;
import nova.common.service.SimpleAddress;

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
		LinkedList<String> softList = msg.getInstallSoftList();
		for (String softName : softList) {
			GlobalPara.downloadBuffer.write(softName);
		}
		// System.out.println("get message!");
	}
}
