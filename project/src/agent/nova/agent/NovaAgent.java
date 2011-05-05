package nova.agent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.daemons.AgentHeartbeatDaemon;
import nova.agent.daemons.AgentPerfInfoDaemon;
import nova.agent.daemons.PackageDownloadDaemon;
import nova.agent.daemons.PackageInstallDaemon;
import nova.agent.handler.AgentRequestHeartbeatHandler;
import nova.agent.handler.AgentRequestPerfHandler;
import nova.agent.handler.RequestSoftwareMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.util.SimpleDaemon;

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
			new PackageDownloadDaemon(), new PackageInstallDaemon(),
			new AgentHeartbeatDaemon(), new AgentPerfInfoDaemon() };

	/**
	 * Start a server and register some handler.
	 */
	public NovaAgent() {
		registerHandler(QueryHeartbeatMessage.class,
				new AgentRequestHeartbeatHandler());
		registerHandler(QueryPerfMessage.class, new AgentRequestPerfHandler());
		registerHandler(QueryApplianceStatusMessage.class,
				new RequestSoftwareMessageHandler());
	}

	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
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

		try {
			logger.info("Nova agent running @ "
					+ InetAddress.getLocalHost().getHostAddress());

			// santa: bind to 0.0.0.0, so master could always connect to agent
			// TODO @gaotao load bind address from conf!

			NovaAgent.getInstance().bind(
					new InetSocketAddress(GlobalPara.AGENT_BIND_HOST,
							GlobalPara.AGENT_BIND_PORT));
		} catch (UnknownHostException ex) {
			logger.fatal("Error booting agent", ex);
		}

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				NovaAgent.getInstance().shutdown();
				logger.info("Cleanup agent done");
			}
		});
	}
}
