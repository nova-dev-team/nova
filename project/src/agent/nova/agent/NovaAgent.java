package nova.agent;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.AgentServer;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.HeartbeatProxy;
import nova.common.service.SimpleAddress;

/**
 * Agent implementation
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class NovaAgent {
	public static void main(String[] args) {
		AgentServer.startServer();

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
