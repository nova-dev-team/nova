package nova.storage;

import nova.common.service.SimpleServer;
import nova.common.util.Conf;
import nova.storage.ftp.FtpUserManager;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.log4j.Logger;

public class NovaStorage extends SimpleServer {

	static Logger logger = Logger.getLogger(NovaStorage.class);

	FtpServer ftpServer = null;

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaStorage() {
		Conf.setDefaultValue("storage.engine", "ftp");
		Conf.setDefaultValue("storage.ftp.bind_host", "0.0.0.0");
		Conf.setDefaultValue("storage.ftp.bind_port", 8021);
		Conf.setDefaultValue("storage.ftp.home", "data/ftp_home");
		Conf.setDefaultValue("storage.ftp.idle_time", 60);
		Conf.setDefaultValue("storage.ftp.admin.username", "admin");
		Conf.setDefaultValue("storage.ftp.admin.password", "liquid");
	}

	public void startFtpServer() {
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		serverFactory.setUserManager(new FtpUserManager());

		// set listener address
		logger.info("FTP server will be running @ "
				+ Conf.getString("storage.ftp.bind_host") + ":"
				+ Conf.getInteger("storage.ftp.bind_port"));

		factory.setServerAddress(Conf.getString("storage.ftp.bind_host"));
		factory.setPort(Conf.getInteger("storage.ftp.bind_port"));

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		this.ftpServer = serverFactory.createServer();

		// start the server
		try {
			this.ftpServer.start();
		} catch (FtpException e) {
			logger.fatal("Error starting FTP server", e);
			System.exit(1);
		}
	}

	/**
	 * Override the shutdown() function, do a few housekeeping work.
	 */
	@Override
	public void shutdown() {
		logger.info("Shutting down NovaStorage");
		super.shutdown();
		this.ftpServer.stop();
	}

	/**
	 * Singleton instance of NovaStorage.
	 */
	private static NovaStorage instance = null;

	/**
	 * Get the singleton of NovaStorage.
	 * 
	 * @return NovaStorage instance, singleton.
	 */
	public static synchronized NovaStorage getInstance() {
		if (NovaStorage.instance == null) {
			NovaStorage.instance = new NovaStorage();
		}
		return NovaStorage.instance;
	}

	public static void main(String[] args) {

		// TODO @future support protocols other than ftp?

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (NovaStorage.instance != null) {
					// do cleanup work
					this.setName("cleanup");
					NovaStorage.getInstance().shutdown();
					logger.info("Cleanup work done");
				}
			}
		});

		NovaStorage.getInstance().startFtpServer();
	}
}
