package nova.worker;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;
import nova.worker.daemons.HeartbeatDaemon;
import nova.worker.daemons.MonitorInfoDaemon;
import nova.worker.daemons.ReportVnodeStatusDeamon;
import nova.worker.handler.StartVnodeHandler;
import nova.worker.handler.WorkerHttpRequestHandler;
import nova.worker.handler.WorkerRequestHeartbeatMessageHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

/**
 * The worker module of Nova.
 * 
 * @author santa
 * 
 */
public class NovaWorker extends SimpleServer {

	InetSocketAddress bindAddr = null;

	/**
	 * All background working daemons for worker node.
	 */
	SimpleDaemon daemons[] = { new HeartbeatDaemon(), new MonitorInfoDaemon(),
			new ReportVnodeStatusDeamon() };

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
		this.registerHandler(DefaultHttpRequest.class,
				new WorkerHttpRequestHandler());

		this.registerHandler(StartVnodeHandler.Message.class,
				new StartVnodeHandler());

		this.registerHandler(RequestHeartbeatMessage.class,
				new WorkerRequestHeartbeatMessageHandler());
	}

	/**
	 * Override the bind() function, do a few housekeeping work.
	 */
	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
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
		logger.info("Shutting down NovaWorker");
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

	public void registerMaster(SimpleAddress xreply) {
		this.master = new MasterProxy(this.bindAddr);
		master.connect(xreply.getInetSocketAddress());
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
	public static NovaWorker getInstance() {
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
		// TODO @shayf Move bind addr into conf files.
		InetSocketAddress bindAddr = new InetSocketAddress("0.0.0.0", 4000);
		logger.info("Nova worker running @ " + bindAddr);
		try {
			NovaWorker.getInstance().bind(bindAddr);
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
				NovaWorker.getInstance().shutdown();
				logger.info("Cleanup work done");
			}
		});
	}

}
