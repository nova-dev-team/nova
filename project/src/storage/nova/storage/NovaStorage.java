package nova.storage;

import nova.common.service.SimpleServer;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.log4j.Logger;

public class NovaStorage extends SimpleServer {

	static Logger logger = Logger.getLogger(NovaStorage.class);

	public static void main(String[] args) {
		// TODO @santa setup bind port, username, password, home folder, etc

		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();

		// set the port of the listener
		factory.setPort(2221);

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		final FtpServer server = serverFactory.createServer();

		// start the server
		try {
			server.start();
		} catch (FtpException e) {
			logger.fatal(e);
			e.printStackTrace();
		}

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
	}
}
