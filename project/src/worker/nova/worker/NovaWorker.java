package nova.worker;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.api.messages.RevokeImageMessage;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.daemons.VdiskPoolDaemon;
import nova.worker.daemons.VnodeStatusDaemon;
import nova.worker.daemons.WorkerHeartbeatDaemon;
import nova.worker.daemons.WorkerPerfInfoDaemon;
import nova.worker.handler.RevokeImageHandler;
import nova.worker.handler.StartVnodeHandler;
import nova.worker.handler.StopVnodeHandler;
import nova.worker.handler.WorkerHttpHandler;
import nova.worker.handler.WorkerQueryHeartbeatHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

/**
 * The worker module of Nova.
 * 
 * @author santa
 * 
 */
public class NovaWorker extends SimpleServer {

	SimpleAddress addr = new SimpleAddress(Conf.getString("worker.bind_host"),
			Conf.getInteger("worker.bind_port"));

	/**
	 * All background working daemons for worker node.
	 */
	SimpleDaemon daemons[] = { new WorkerHeartbeatDaemon(),
			new WorkerPerfInfoDaemon(), new VnodeStatusDaemon(),
			new VdiskPoolDaemon() };

	/**
	 * Connection to nova master.
	 */
	MasterProxy master = null;

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaWorker() {
		// TODO @shayf register handlers

		// handle http requests
		this.registerHandler(DefaultHttpRequest.class, new WorkerHttpHandler());

		this.registerHandler(StartVnodeMessage.class, new StartVnodeHandler());

		this.registerHandler(StopVnodeMessage.class, new StopVnodeHandler());

		this.registerHandler(QueryHeartbeatMessage.class,
				new WorkerQueryHeartbeatHandler());

		this.registerHandler(RevokeImageMessage.class, new RevokeImageHandler());

		Conf.setDefaultValue("worker.bind_host", "0.0.0.0");
		Conf.setDefaultValue("worker.bind_port", 4000);

	}

	public SimpleAddress getAddr() {
		return this.addr;
	}

	public Channel start() {
		logger.info("Nova worker running @ " + this.addr);
		Channel chnl = super.bind(this.addr.getInetSocketAddress());
		// start all daemons
		for (SimpleDaemon daemon : this.daemons) {
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
		logger.info("Shutting down NovaWorker");
		// stop all daemons
		for (SimpleDaemon daemon : this.daemons) {
			daemon.stopWork();
		}
		for (SimpleDaemon daemon : this.daemons) {
			try {
				daemon.join();
			} catch (InterruptedException e) {
				logger.error("Error joining thread " + daemon.getName(), e);
			}
		}
		logger.info("All deamons stopped");
		super.shutdown();
		// TODO @shayf more cleanup work

		NovaWorker.instance = null;
	}

	/**
	 * Get current master proxy.
	 * 
	 * @return Current master proxy. Could be <code>NULL</code>, when no master
	 *         node has connected.
	 */
	public MasterProxy getMaster() {
		return this.master;
	}

	public void registerMaster(SimpleAddress masterAddr) {
		MasterProxy proxy = new MasterProxy(this.addr);
		proxy.connect(masterAddr.getInetSocketAddress());
		this.master = proxy;
	}

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(NovaWorker.class);

	/**
	 * Singleton instance of NovaWorker.
	 */
	private static NovaWorker instance = null;

	/**
	 * Get the singleton of NovaWorker.
	 * 
	 * @return NovaWorker instance, singleton.
	 */
	public static synchronized NovaWorker getInstance() {
		if (NovaWorker.instance == null) {
			NovaWorker.instance = new NovaWorker();
		}
		return NovaWorker.instance;
	}

	/**
	 * Application entry of NovaWorker.
	 * 
	 * @param args
	 *            Environment variables.
	 */
	public static void main(String[] args) {
		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (NovaWorker.instance != null) {
					// do cleanup work
					this.setName("cleanup");
					NovaWorker.getInstance().shutdown();
					logger.info("Cleanup work done");
				}
			}
		});

		String bindHost = Conf.getString("worker.bind_host");
		Integer bindPort = Conf.getInteger("worker.bind_port");
		InetSocketAddress bindAddr = new InetSocketAddress(bindHost, bindPort);
		NovaWorker.getInstance().bind(bindAddr);

	}
}
