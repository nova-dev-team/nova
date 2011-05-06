package nova.agent;

import java.io.IOException;
import java.net.InetSocketAddress;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.daemons.AgentHeartbeatDaemon;
import nova.agent.daemons.AgentPerfInfoDaemon;
import nova.agent.daemons.PackageDownloadDaemon;
import nova.agent.daemons.PackageInstallDaemon;
import nova.agent.handler.AgentQueryHeartbeatHandler;
import nova.agent.handler.AgentQueryPerfHandler;
import nova.agent.handler.QueryApplianceStatusHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * The agent server model of nova
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class NovaAgent extends SimpleServer {

	/**
	 * Config info for agent.
	 */
	Conf conf = null;

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(NovaAgent.class);

	/**
	 * Singleton instance of AgentServer.
	 */
	private static NovaAgent instance = null;

	InetSocketAddress bindAddr = null;

	/**
	 * All background working daemons for agent node.
	 */
	public SimpleDaemon[] daemons = new SimpleDaemon[] {
			new PackageDownloadDaemon(), PackageInstallDaemon.getInstance(),
			new AgentHeartbeatDaemon(), new AgentPerfInfoDaemon() };

	/**
	 * Connection to nova master.
	 */
	MasterProxy master = null;

	/**
	 * Start a server and register some handler.
	 */
	private NovaAgent() {
		registerHandler(QueryHeartbeatMessage.class,
				new AgentQueryHeartbeatHandler());
		registerHandler(QueryPerfMessage.class, new AgentQueryPerfHandler());
		registerHandler(QueryApplianceStatusMessage.class,
				new QueryApplianceStatusHandler());
	}

	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
		logger.info("Nova agent running @ " + this.bindAddr);
		Channel chnl = super.bind(this.bindAddr);
		for (SimpleDaemon daemon : this.daemons) {
			daemon.start();
		}
		return chnl;
	}

	/**
	 * Override the shutdown() function, do a few housekeeping work.
	 */
	@Override
	public void shutdown() {
		logger.info("Shutting down AgentServer");
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
		this.bindAddr = null;
		NovaAgent.instance = null;
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

	public void registerMaster(SimpleAddress xreply) {
		// FIXME @santa: bindAddr should not be 0.0.0.0!
		this.master = new MasterProxy(this.bindAddr);
		master.connect(xreply.getInetSocketAddress());
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

	/**
	 * Get the singleton of AgentServer.
	 * 
	 * @return AgentServer instance, singleton.
	 */
	public static synchronized NovaAgent getInstance() {
		if (NovaAgent.instance == null) {
			NovaAgent.instance = new NovaAgent();
		}
		return NovaAgent.instance;
	}

	public static void main(String[] args) {

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				this.setName("cleanup");
				NovaAgent.getInstance().shutdown();
				logger.info("Cleanup work done");
			}
		});

		try {
			Conf conf = Utils.loadConf();
			NovaAgent.getInstance().setConf(conf);
			String bindHost = conf.getString("agent.bind_host");
			Integer bindPort = conf.getInteger("agent.bind_port");
			InetSocketAddress bindAddr = new InetSocketAddress(bindHost,
					bindPort);
			NovaAgent.getInstance().bind(bindAddr);
		} catch (IOException e) {
			logger.fatal("Error booting master", e);
			System.exit(1);
		}

	}
}
