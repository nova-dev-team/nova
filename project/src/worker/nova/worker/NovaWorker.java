package nova.worker;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.api.messages.InstallApplianceMessage;
import nova.worker.api.messages.MigrateVnodeMessage;
import nova.worker.api.messages.QueryPnodeInfoMessage;
import nova.worker.api.messages.QueryVnodeInfoMessage;
import nova.worker.api.messages.RevokeImageMessage;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.daemons.PnodeStatusDaemon;
import nova.worker.daemons.VdiskPoolDaemon;
import nova.worker.daemons.VnodeStatusDaemon;
import nova.worker.daemons.WorkerHeartbeatDaemon;
import nova.worker.daemons.WorkerPerfInfoDaemon;
import nova.worker.handler.InstallApplianceHandler;
import nova.worker.handler.MigrateVnodeHandler;
import nova.worker.handler.RevokeImageHandler;
import nova.worker.handler.StartVnodeHandler;
import nova.worker.handler.StopVnodeHandler;
import nova.worker.handler.WorkerHttpHandler;
import nova.worker.handler.WorkerQueryHeartbeatHandler;
import nova.worker.handler.WorkerQueryPnodeInfoMessageHandler;
import nova.worker.handler.WorkerQueryVnodeInfoMessageHandler;
import nova.worker.models.StreamGobbler;

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
			new VdiskPoolDaemon(), new PnodeStatusDaemon() };

	/**
	 * Connection to nova master.
	 */
	MasterProxy master = null;

	/**
	 * currently installed app list
	 */
	HashMap<String, String> appStatus = new HashMap<String, String>();

	/**
	 * vnode ip address
	 */
	HashMap<UUID, String> vnodeIP = new HashMap<UUID, String>();

	public HashMap<UUID, String> getVnodeIP() {
		return vnodeIP;
	}

	public void setVnodeIP(HashMap<UUID, String> vnodeIP) {
		this.vnodeIP = vnodeIP;
	}

	public HashMap<String, String> getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(HashMap<String, String> appStatus) {
		this.appStatus = appStatus;
	}

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

		this.registerHandler(QueryPnodeInfoMessage.class,
				new WorkerQueryPnodeInfoMessageHandler());

		this.registerHandler(QueryVnodeInfoMessage.class,
				new WorkerQueryVnodeInfoMessageHandler());

		this.registerHandler(InstallApplianceMessage.class,
				new InstallApplianceHandler());

		this.registerHandler(MigrateVnodeMessage.class,
				new MigrateVnodeHandler());

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

		// create br0
		String[] createBridgeCmds = {
				"ifconfig br0 down",
				"brctl delbr br0",
				"brctl addbr br0",
				"brctl setbridgeprio br0 0",
				"brctl addif br0 eth0",
				"ifconfig eth0 0.0.0.0",
				"ifconfig br0 " + Conf.getString("worker.bind_host")
						+ " netmask " + Conf.getString("worker.bind_host"),
				"brctl sethello br0 1", "brctl setmaxage br0 4",
				"brctl setfd br0 4", "ifconfig br0 up",
				"route add default gw " + Conf.getString("worker.gateway") };

		Process p;
		try {
			for (String cmd : createBridgeCmds) {
				p = Runtime.getRuntime().exec(cmd);
				StreamGobbler errorGobbler = new StreamGobbler(
						p.getErrorStream(), "ERROR");
				errorGobbler.start();
				StreamGobbler outGobbler = new StreamGobbler(
						p.getInputStream(), "STDOUT");
				outGobbler.start();
				try {
					if (p.waitFor() != 0) {
						logger.error("create bridge returned abnormal value!");
					}
				} catch (InterruptedException e1) {
					logger.error("create bridge terminated", e1);
				}
			}
		} catch (IOException e1) {
			logger.error("create bridge cmd error!", e1);
		}

		NovaWorker.getInstance().start();
	}
}
