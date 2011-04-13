package nova.agent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.AgentServer;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.HeartbeatProxy;
import nova.common.service.SimpleAddress;

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

		Thread heartbeatThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (GlobalPara.heartbeatProxyMap.isEmpty())
					synchronized (GlobalPara.heartbeatSem) {
						try {
							GlobalPara.heartbeatSem.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				while (!GlobalPara.heartbeatProxyMap.isEmpty()) {
					for (SimpleAddress xreply : GlobalPara.heartbeatProxyMap
							.keySet()) {
						HeartbeatProxy heartbeatProxy = GlobalPara.heartbeatProxyMap
								.get(xreply);
						heartbeatProxy.sendHeartbeatMessage();
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		heartbeatThread.start();

		Thread generalMonitorThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (GlobalPara.generalMonitorProxyMap.isEmpty())
					synchronized (GlobalPara.generalMonitorSem) {
						try {
							GlobalPara.generalMonitorSem.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				while (!GlobalPara.generalMonitorProxyMap.isEmpty()) {
					for (SimpleAddress xreply : GlobalPara.generalMonitorProxyMap
							.keySet()) {
						GeneralMonitorProxy generalMonitorProxy = GlobalPara.generalMonitorProxyMap
								.get(xreply);
						generalMonitorProxy.sendGeneralMonitorMessage();
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		generalMonitorThread.start();
	}
}
