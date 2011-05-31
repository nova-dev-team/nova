package nova.master;

import java.net.InetSocketAddress;
import java.util.HashMap;

import nova.common.db.HibernateUtil;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.common.service.message.PerfMessage;
import nova.common.service.message.PnodeHeartbeatMessage;
import nova.common.service.message.VnodeHeartbeatMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.daemons.PnodeCheckerDaemon;
import nova.master.handler.AddPnodeHandler;
import nova.master.handler.MasterAgentHeartbeatHandler;
import nova.master.handler.CreateVclusterHandler;
import nova.master.handler.CreateVnodeHandler;
import nova.master.handler.MasterHttpHandler;
import nova.master.handler.MasterPerfHandler;
import nova.master.handler.MasterPnodeHeartbeatHandler;
import nova.master.handler.MasterVnodeHeartbeatHandler;
import nova.master.handler.PnodeStatusHandler;
import nova.master.handler.RegisterApplianceHandler;
import nova.master.handler.RegisterVdiskHandler;
import nova.master.handler.VnodeStatusHandler;
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

	SimpleAddress addr = new SimpleAddress(Conf.getString("master.bind_host"),
			Conf.getInteger("master.bind_port"));

	/**
	 * All background working daemons for master node.
	 */
	SimpleDaemon daemons[] = { new PnodeCheckerDaemon() };

	HashMap<SimpleAddress, WorkerProxy> workerProxyPool = new HashMap<SimpleAddress, WorkerProxy>();

	/**
	 * Constructor made private for singleton pattern.
	 */
	private NovaMaster() {

		// register handlers

		// handle http requests
		this.registerHandler(DefaultHttpRequest.class, new MasterHttpHandler());

		this.registerHandler(PnodeHeartbeatMessage.class,
				new MasterPnodeHeartbeatHandler());

		this.registerHandler(VnodeHeartbeatMessage.class,
				new MasterVnodeHeartbeatHandler());

		this.registerHandler(AgentHeartbeatMessage.class,
				new MasterAgentHeartbeatHandler());

		this.registerHandler(AddPnodeMessage.class, new AddPnodeHandler());

		this.registerHandler(CreateVnodeMessage.class, new CreateVnodeHandler());

		this.registerHandler(CreateVclusterMessage.class,
				new CreateVclusterHandler());

		this.registerHandler(RegisterVdiskMessage.class,
				new RegisterVdiskHandler());

		this.registerHandler(RegisterApplianceMessage.class,
				new RegisterApplianceHandler());

		this.registerHandler(PnodeStatusMessage.class, new PnodeStatusHandler());

		this.registerHandler(VnodeStatusMessage.class, new VnodeStatusHandler());

		this.registerHandler(PerfMessage.class, new MasterPerfHandler());

		Conf.setDefaultValue("master.bind_host", "0.0.0.0");
		Conf.setDefaultValue("master.bind_port", 3000);
	}

	public SimpleAddress getAddr() {
		return this.addr;
	}

	/**
	 * Start server.
	 * 
	 * @return
	 */
	public Channel start() {
		logger.info("Nova master running @ " + this.addr);
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
		logger.info("Shutting down NovaMaster");
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
		this.addr = null;
		// TODO @zhaoxun more cleanup work
		HibernateUtil.shutdown();

		NovaMaster.instance = null;
	}

	public WorkerProxy getWorkerProxy(final SimpleAddress pAddr) {
		if (workerProxyPool.get(pAddr) == null) {
			WorkerProxy wp = new WorkerProxy(this.addr) {

				@Override
				public void exceptionCaught(ChannelHandlerContext ctx,
						ExceptionEvent e) {
					Pnode pnode = Pnode.findByIp(pAddr.ip);
					if (pnode != null) {
						pnode.setStatus(Pnode.Status.CONNECT_FAILURE);
						logger.info("Update status of pnode @ "
								+ pnode.getAddr() + " to " + pnode.getStatus());
						pnode.save();
					} else {
						logger.error("Pnode with host " + pAddr.ip
								+ " not found!");
					}
					super.exceptionCaught(ctx, e);
				}

			};
			wp.connect(new InetSocketAddress(pAddr.getIp(), pAddr.getPort()));
			workerProxyPool.put(pAddr, wp);
		}
		return workerProxyPool.get(pAddr);
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
				if (NovaMaster.instance != null) {
					// do cleanup work
					this.setName("cleanup");
					NovaMaster.getInstance().shutdown();
					logger.info("Cleanup work done");
				}
			}

		});

		NovaMaster.getInstance().start();

	}
}
