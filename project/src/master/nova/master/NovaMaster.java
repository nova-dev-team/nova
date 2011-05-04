package nova.master;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.daemons.PnodeHealthCheckerDaemon;
import nova.master.handler.MasterGeneralMonitorMessageHandler;
import nova.master.handler.MasterHeartbeatHandler;
import nova.master.handler.MasterHttpRequestHandler;
import nova.master.handler.PnodeStatusMessageHandler;
import nova.master.handler.VnodeStatusMessageHandler;
import nova.master.models.MasterDB;
import nova.master.models.Pnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

/**
 * Master node of Nova system.
 * 
 * @author santa
 * 
 */
public class NovaMaster extends SimpleServer {

	Conf conf = null;

	InetSocketAddress bindAddr = null;

	/**
	 * All background working daemons for master node.
	 */
	SimpleDaemon daemons[] = { new PnodeHealthCheckerDaemon() };

	/**
	 * Master's db.
	 */
	MasterDB db = new MasterDB();

	HashMap<SimpleAddress, WorkerProxy> workerProxyPool = new HashMap<SimpleAddress, WorkerProxy>();

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaMaster() {
		// register handlers

		// handle http requests
		this.registerHandler(DefaultHttpRequest.class,
				new MasterHttpRequestHandler());

		this.registerHandler(HeartbeatMessage.class,
				new MasterHeartbeatHandler());

		this.registerHandler(PnodeStatusMessage.class,
				new PnodeStatusMessageHandler());

		this.registerHandler(VnodeStatusMessage.class,
				new VnodeStatusMessageHandler());

		this.registerHandler(GeneralMonitorMessage.class,
				new MasterGeneralMonitorMessageHandler());

		// TODO @zhaoxun connect db
	}

	/**
	 * Override the bind() function, do a few housekeeping work.
	 */
	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
		logger.info("Nova master running @ " + this.bindAddr);
		Channel chnl = super.bind(this.bindAddr);
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
		logger.info("Shutting down NovaMaster");
		// stop all daemons
		for (SimpleDaemon daemon : this.daemons) {
			daemon.stopWork();
		}
		for (SimpleDaemon daemon : this.daemons) {
			try {
				daemon.join();
			} catch (InterruptedException e) {
				logger.error("Error joining thread '" + daemon.getName() + "'",
						e);
			}
		}
		logger.info("All deamons stopped");
		super.shutdown();
		this.bindAddr = null;
		// TODO @zhaoxun more cleanup work

		NovaMaster.instance = null;
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

	/**
	 * Get master's database.
	 * 
	 * @return Master's database.
	 */
	public MasterDB getDB() {
		return this.db;
	}

	public WorkerProxy getWorkerProxy(final SimpleAddress pAddr) {
		if (workerProxyPool.get(pAddr) == null) {
			WorkerProxy wp = new WorkerProxy(this.bindAddr) {

				@Override
				public void exceptionCaught(ChannelHandlerContext ctx,
						ExceptionEvent e) {
					getDB().updatePnodeStatus(pAddr,
							Pnode.Status.CONNECT_FAILURE);
					super.exceptionCaught(ctx, e);
				}

			};
			wp.connect(new InetSocketAddress(pAddr.getIp(), pAddr.getPort()));
			workerProxyPool.put(pAddr, wp);
		}
		return workerProxyPool.get(pAddr);
	}

	/**
	 * Log exception.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error(e.getCause());
		super.exceptionCaught(ctx, e);
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
	public static synchronized NovaMaster getInstance() {
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

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				// do cleanup work
				this.setName("cleanup");
				NovaMaster.getInstance().shutdown();
				logger.info("Cleanup work done");
			}

		});

		try {
			Conf conf = Utils.loadConf();
			NovaMaster.getInstance().setConf(conf);
			String bindHost = conf.getString("master.bind_host");
			Integer bindPort = conf.getInteger("master.bind_port");
			InetSocketAddress bindAddr = new InetSocketAddress(bindHost,
					bindPort);
			NovaMaster.getInstance().bind(bindAddr);
		} catch (IOException e) {
			logger.fatal("Error booting master", e);
			System.exit(1);
		}

	}
}
