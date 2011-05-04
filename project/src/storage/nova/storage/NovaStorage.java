package nova.storage;

import java.io.File;
import java.io.IOException;

import nova.common.service.SimpleServer;
import nova.common.util.Conf;
import nova.common.util.Utils;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.Logger;

public class NovaStorage extends SimpleServer {

	static Logger logger = Logger.getLogger(NovaStorage.class);

	public static void main(String[] args) {
		Conf conf = null;
		try {
			conf = Utils.loadConf();
		} catch (IOException e) {
			logger.fatal("Failed to load config file", e);
			System.exit(1);
		}

		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();

		// set users
		String userAccountFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf",
				"storage.ftp.users.properties");
		logger.info("User account file: " + userAccountFpath);
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setFile(new File(userAccountFpath));
		serverFactory.setUserManager(userManagerFactory.createUserManager());

		// set listener address
		logger.info("FTP server will be running @ "
				+ conf.getString("storage.ftp.bind_host") + ":"
				+ conf.getInteger("storage.ftp.bind_port"));

		factory.setServerAddress(conf.getString("storage.ftp.bind_host"));
		factory.setPort(conf.getInteger("storage.ftp.bind_port"));

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		final FtpServer server = serverFactory.createServer();

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				server.stop();
				logger.info("FTP server stopped");
			}
		});

		// start the server
		try {
			server.start();
		} catch (FtpException e) {
			logger.fatal("Error starting FTP server", e);
			System.exit(1);
		}

	}
}
