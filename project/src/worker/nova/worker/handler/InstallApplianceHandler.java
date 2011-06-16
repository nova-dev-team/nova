package nova.worker.handler;

import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
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
		// TODO @shayf download softwares and pack an iso file
		for (String appName : msg.appNames) {
			if (NovaWorker.getInstance().getAppStatus().containsKey(appName) == false) {
				try {
					FtpClient fc = FtpUtils.connect(
							Conf.getString("storage.ftp.bind_host"),
							Conf.getInteger("storage.ftp.bind_port"),
							Conf.getString("storage.ftp.admin.username"),
							Conf.getString("storage.ftp.admin.password"));
					fc.cd("appliances");
					FtpUtils.downloadDir(fc, Utils.pathJoin(appName),
							Utils.pathJoin(Utils.NOVA_HOME, "run", appName));
					System.out.println("download file " + appName);
					fc.closeServer();
				} catch (NumberFormatException e1) {
					log.error("port format error!", e1);
				} catch (IOException e1) {
					log.error("ftp connection fail!", e1);
				}
			}
		}

		// TODO @shayf pack ISO files
	}
}
