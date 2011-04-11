package nova.master;

import java.net.InetSocketAddress;

import nova.common.service.SimpleServer;
import nova.master.daemons.MasterDaemon;
import nova.master.daemons.PnodeHealthCheckerDaemon;
import nova.master.handler.AckStartVnodeHandler;
import nova.master.models.MasterDB;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;

/**
 * Master node of Nova system.
 * 
 * @author santa
 * 
 */
public class NovaMaster extends SimpleServer {

	/**
	 * All background working daemons for master node.
	 */
	MasterDaemon daemons[] = { new PnodeHealthCheckerDaemon() };

	/**
	 * Master's db.
	 */
	MasterDB db = new MasterDB();

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaMaster() {

		// register handlers
		this.registerHandler(AckStartVnodeHandler.Message.class,
				new AckStartVnodeHandler());

		// TODO @santa connect db
	}

	/**
	 * Override the bind() function, do a few housekeeping work.
	 */
	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		Channel chnl = super.bind(bindAddr);
		// start all daemons
		for (MasterDaemon daemon : this.daemons) {
			daemon.start();
		}
		logger.info("All deamons started");
		return chnl;
	}

	/**
	 * Override the shutdown() function, do a few housekeeping work.
	 */
	@Override
	public void shutdown() {
		logger.info("Shutting down NovaMaster");
		// stop all daemons
		for (MasterDaemon daemon : this.daemons) {
			daemon.stopWork();
		}
		for (MasterDaemon daemon : this.daemons) {
			try {
				daemon.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.info("All deamons stopped");
		super.shutdown();
		// TODO @santa more cleanup work

		NovaMaster.instance = null;
	}

	/**
	 * Get master's database.
	 * 
	 * @return Master's database.
	 */
	public MasterDB getDB() {
		return this.db;
	}

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(NovaMaster.class);

	/**
	 * Singleton instance of NovaMaster.
	 */
	private static NovaMaster instance = null;

	/**
	 * Get the singleton of NovaMaster.
	 * 
	 * @return NovaMaster instance, singleton.
	 */
	public static NovaMaster getInstance() {
		if (NovaMaster.instance == null) {
			NovaMaster.instance = new NovaMaster();
		}
		return NovaMaster.instance;
	}

	/**
	 * Application entry of NovaMaster.
	 * 
	 * @param args
	 *            Environment variables.
	 */
	public static void main(String[] args) {
		// TODO @santa Move bind addr into conf files.
		InetSocketAddress bindAddr = new InetSocketAddress("0.0.0.0", 3000);
		logger.info("Nova master running @ " + bindAddr);
		try {
			NovaMaster.getInstance().bind(bindAddr);
		} catch (ChannelException e) {
			e.printStackTrace();
			logger.fatal(e);
		}

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				NovaMaster.getInstance().shutdown();
				logger.info("Cleanup work done");
			}
		});
	}
}
