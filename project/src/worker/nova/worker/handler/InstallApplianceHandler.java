package nova.worker.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.storage.NovaStorage;
import nova.worker.NovaWorker;
import nova.worker.api.messages.InstallApplianceMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import sun.net.ftp.FtpClient;

/**
 * handler to prepare softwares
 * 
 * @author syf
 * 
 */
public class InstallApplianceHandler implements
		SimpleHandler<InstallApplianceMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(InstallApplianceHandler.class);

	@Override
	public void handleMessage(InstallApplianceMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		File pathfile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
				"softwares"));
		if (!pathfile.exists()) {
			Utils.mkdirs(pathfile.getAbsolutePath());
		}
		for (String appName : msg.appNames) {
			if (NovaWorker.getInstance().getAppStatus().containsKey(appName) == false) {
				try {
					if (NovaStorage.getInstance().getFtpServer() == null) {
						NovaStorage.getInstance().startFtpServer();
					}
					FtpClient fc = FtpUtils.connect(
							Conf.getString("storage.ftp.bind_host"),
							Conf.getInteger("storage.ftp.bind_port"),
							Conf.getString("storage.ftp.admin.username"),
							Conf.getString("storage.ftp.admin.password"));
					fc.cd("appliances");
					FtpUtils.downloadDir(fc, Utils.pathJoin(appName), Utils
							.pathJoin(Utils.NOVA_HOME, "run", "softwares",
									appName));
					System.out.println("download file " + appName);
					fc.closeServer();
				} catch (NumberFormatException e1) {
					log.error("port format error!", e1);
				} catch (IOException e1) {
					log.error("ftp connection fail!", e1);
				}
				NovaWorker.getInstance().getAppStatus().put(appName, appName);
			}
		}

		File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
				"agentcd"));
		if (!agentCdFile.exists()) {
			Utils.mkdirs(agentCdFile.getAbsolutePath());
		}
		System.out.println("packing iso");
		Process p;
		String cmd = "mkisofs -J -T -R -V agent -o "
				+ Utils.pathJoin(Utils.NOVA_HOME, "run", "agentcd",
						"agent-cd.iso") + " "
				+ Utils.pathJoin(Utils.NOVA_HOME, "run", "softwares");
		System.out.println(cmd);
		try {
			p = Runtime.getRuntime().exec(cmd);
			InputStream fis = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
