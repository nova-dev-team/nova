package nova.agent.core.handler;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.DownloadProgress;
import nova.agent.core.InstallProgress;
import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.RequestSoftwareMessage;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Download and install softwares in receive soft list.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RequestSoftwareMessageHandler implements
		ISimpleHandler<RequestSoftwareMessage> {
	public AtomicLong counter = new AtomicLong();

	public ExecutorService softDownloadPool = Executors.newFixedThreadPool(1);
	public ExecutorService softInstallPool = Executors.newFixedThreadPool(1);

	@Override
	public void handleMessage(RequestSoftwareMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress reply) {
		LinkedList<String> softList = msg.getInstallSoftList();

		// add all downloading softwares task to download thread pool
		for (String softName : softList) {
			DownloadProgress dlp = new DownloadProgress(GlobalPara.hostIp,
					GlobalPara.userName, GlobalPara.password, softName);
			softDownloadPool.execute(dlp);
		}
		softDownloadPool.shutdown();

		// add all downloaded software to install thread pool
		while (!softDownloadPool.isTerminated()) { // Can be changed to
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			String downloadedSoftware = GlobalPara.downloadedBuffer.read();

			InstallProgress insP = new InstallProgress(downloadedSoftware,
					GlobalPara.myPath);
			softInstallPool.execute(insP);
		}

		while (!GlobalPara.downloadedBuffer.isEmpty()) {
			String downloadedSoftware = GlobalPara.downloadedBuffer.read();

			InstallProgress insP = new InstallProgress(downloadedSoftware,
					GlobalPara.myPath);
			softInstallPool.execute(insP);
		}

		softInstallPool.shutdown();
	}
}
