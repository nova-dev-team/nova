package nova.storage;

import java.io.IOException;

import nova.common.service.SimpleServer;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.storage.ftp.FtpUserManager;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.log4j.Logger;

public class NovaStorage extends SimpleServer {

	static Logger logger = Logger.getLogger(NovaStorage.class);

	/**
	 * Config info for storage server.
	 */
	Conf conf = null;

	FtpServer ftpServer = null;

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaStorage() {

	}

	/**
	 * Get config info.
	 * 
	 * @return The config info.
	 */
	public Conf getConf() {
		return this.conf;
	}

	/**
	 * Set config info.
	 * 
	 * @param conf
	 *            The new config info.
	 */
	public void setConf(Conf conf) {
		this.conf = conf;
	}

	public void startFtpServer() {
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		serverFactory.setUserManager(new FtpUserManager());

		// set listener address
		logger.info("FTP server will be running @ "
				+ conf.getString("storage.ftp.bind_host") + ":"
				+ conf.getInteger("storage.ftp.bind_port"));

		factory.setServerAddress(conf.getString("storage.ftp.bind_host"));
		factory.setPort(conf.getInteger("storage.ftp.bind_port"));

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

		// TODO [future] support protocols other than ftp?

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				this.setName("cleanup");
				NovaStorage.getInstance().shutdown();
				logger.info("Cleanup work done");
			}
		});

		try {
			Conf conf = Utils.loadConf();

			conf.setDefaultValue("storage.engine", "ftp");
			conf.setDefaultValue("storage.ftp.bind_host", "0.0.0.0");
			conf.setDefaultValue("storage.ftp.bind_port", 8021);
			conf.setDefaultValue("storage.ftp.home", "data/ftp_home");
			conf.setDefaultValue("storage.ftp.idle_time", 60);
			conf.setDefaultValue("storage.ftp.admin.username", "admin");
			conf.setDefaultValue("storage.ftp.admin.password", "liquid");

			NovaStorage.getInstance().setConf(conf);
			NovaStorage.getInstance().startFtpServer();

		} catch (IOException e) {
			logger.fatal("Failed to load config file", e);
			System.exit(1);
		}

	}
}
