package nova.agent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.AgentServer;

import org.apache.log4j.Logger;

/**
 * Agent implementation
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class NovaAgent {
	static Logger logger = Logger.getLogger(NovaAgent.class);

	public static void main(String[] args) {

		try {
			logger.info("Nova agent running @ "
					+ InetAddress.getLocalHost().getHostAddress());
			AgentServer.getInstance().bind(
					new InetSocketAddress(InetAddress.getLocalHost()
							.getHostAddress(), GlobalPara.BIND_PORT));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			logger.fatal(e1);
		}

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				AgentServer.getInstance().shutdown();
				logger.info("Cleanup agent done");
			}
		});
	}
}
