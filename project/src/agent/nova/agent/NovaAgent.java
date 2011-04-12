package nova.agent;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.service.AgentServer;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.HeartbeatProxy;
import nova.common.service.message.HeartbeatMessage;

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
					for (String xfrom : GlobalPara.heartbeatProxyMap.keySet()) {
						HeartbeatProxy heartbeatProxy = GlobalPara.heartbeatProxyMap
								.get(xfrom);
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
					for (String xfrom : GlobalPara.generalMonitorProxyMap
							.keySet()) {
						GeneralMonitorProxy generalMonitorProxy = GlobalPara.generalMonitorProxyMap
								.get(xfrom);
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

class SynchClass {
	public Object heartbeatSem = new Object();
	public Object generalMonitorSem = new Object();

	public SynchClass(Object klass) {
		if (klass.getClass().getName().equals(HeartbeatMessage.class.getName())) {
			this.heartbeatSem.notifyAll();
		} else {
			this.generalMonitorSem.notifyAll();
		}
	}
}
