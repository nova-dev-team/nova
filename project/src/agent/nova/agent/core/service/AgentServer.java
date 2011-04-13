package nova.agent.core.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.handler.CloseChannelMessageHandler;
import nova.agent.core.handler.HeartbeatMessageHandler;
import nova.agent.core.handler.RequestGeneralMonitorMessageHandler;
import nova.agent.core.handler.RequestHeartbeatMessageHandler;
import nova.agent.core.handler.RequestSoftwareMessageHandler;
import nova.agent.daemons.DownloadProgressDaemon;
import nova.agent.daemons.GeneralMonitorDaemon;
import nova.agent.daemons.HeartbeatDaemon;
import nova.agent.daemons.InstallProgressDaemon;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestGeneralMonitorMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;
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
public class AgentServer extends SimpleServer {
	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(AgentServer.class);

	/**
	 * Singleton instance of AgentServer.
	 */
	private static AgentServer instance = null;

	InetSocketAddress bindAddr = null;

	/**
	 * All background working daemons for agent node.
	 */
	public ArrayList<SimpleDaemon> daemons = new ArrayList<SimpleDaemon>();

	/**
	 * Start a server and register some handler.
	 */
	public AgentServer() {
		registerHandler(HeartbeatMessage.class, new HeartbeatMessageHandler());
		registerHandler(RequestHeartbeatMessage.class,
				new RequestHeartbeatMessageHandler());
		registerHandler(RequestGeneralMonitorMessage.class,
				new RequestGeneralMonitorMessageHandler());
		registerHandler(RequestSoftwareMessage.class,
				new RequestSoftwareMessageHandler());
		registerHandler(CloseChannelMessage.class,
				new CloseChannelMessageHandler());
		/**
		 * DownloadProgressDaemon and installProcessDaemon start
		 */
		DownloadProgressDaemon downloadProgressDaemon = new DownloadProgressDaemon();
		downloadProgressDaemon.start();
		daemons.add(downloadProgressDaemon);

		InstallProgressDaemon installProgressDaemon = new InstallProgressDaemon();
		installProgressDaemon.start();
		daemons.add(installProgressDaemon);

		/**
		 * wait until request heartbeat message comes
		 */
		Thread waitHeartbeatSem = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true)
					synchronized (GlobalPara.heartbeatSem) {
						try {
							GlobalPara.heartbeatSem.wait();
							break;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				HeartbeatDaemon hbDaemon = new HeartbeatDaemon();
				hbDaemon.start();
				daemons.add(hbDaemon);

				logger.info("Heartbeat daemon starts!");
			}
		});
		waitHeartbeatSem.start();

		/**
		 * wait until general monitor message comes
		 */
		Thread waitGeneralMonitorSem = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true)
					synchronized (GlobalPara.generalMonitorSem) {
						try {
							GlobalPara.generalMonitorSem.wait();
							break;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				GeneralMonitorDaemon gmDaemon = new GeneralMonitorDaemon();
				daemons.add(gmDaemon);
				gmDaemon.start();

				logger.info("General monitor daemon starts!");

			}
		});
		waitGeneralMonitorSem.start();
	}

	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
		Channel chnl = super.bind(this.bindAddr);

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
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.info("All deamons stopped");
		super.shutdown();
		this.bindAddr = null;

		AgentServer.instance = null;
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
	public static AgentServer getInstance() {
		if (AgentServer.instance == null) {
			AgentServer.instance = new AgentServer();
		}
		return AgentServer.instance;
	}
}