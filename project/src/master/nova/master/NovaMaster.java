package nova.master;

import java.net.InetSocketAddress;

import nova.common.service.SimpleServer;
import nova.master.daemons.MasterDaemon;
import nova.master.daemons.PnodeHealthCheckerDaemon;
import nova.master.handler.AckStartVnodeHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

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
	 * Constructor made private for singleton pattern.
	 */
	private NovaMaster() {

		// register handlers
		this.registerHandler(AckStartVnodeHandler.Message.class,
				new AckStartVnodeHandler());
	}

	/**
	 * Master's messageReceived function is duplex'ed to handle HTTP requests.
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		String msg = (String) e.getMessage();
		if (isHTTPRequest(msg)) {
			// if it is request from browers, handle the http request.
			handleHTTPRequest(ctx, e, msg);
		} else {
			// otherwise, it is from nova components, handle it by simple
			// server's logics
			super.messageReceived(ctx, e);
		}
	}

	/**
	 * Check if a request looks like HTTP request.
	 * 
	 * @param req
	 *            The request message
	 * @return Whether the request is HTTP request.
	 */
	private boolean isHTTPRequest(String req) {
		if (req.startsWith("GET ") || req.startsWith("POST ")
				|| req.startsWith("PUT ") || req.startsWith("HEAD ")
				|| req.startsWith("DELETE ")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Handle a HTTP request from browsers.
	 * 
	 * @param ctx
	 *            The ChannelHandlerContext.
	 * @param e
	 *            The MessageEvent.
	 * @param req
	 *            The request body.
	 */
	private void handleHTTPRequest(ChannelHandlerContext ctx, MessageEvent e,
			String req) {
		// TODO @santa handle http req
		System.out.println(req);
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
	}

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(NovaMaster.class);

	/**
	 * Singleton instance of NovaMaster.
	 */
	private static NovaMaster instance = new NovaMaster();

	/**
	 * Get the singleton of NovaMaster.
	 * 
	 * @return NovaMaster instance, singleton.
	 */
	public static NovaMaster getInstance() {
		return NovaMaster.instance;
	}

	/**
	 * Application entry of NovaMaster.
	 * 
	 * @param args
	 *            Environment variables.
	 */
	public static void main(String[] args) {
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
