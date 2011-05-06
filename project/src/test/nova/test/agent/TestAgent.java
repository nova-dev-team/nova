package nova.test.agent;

import java.net.InetSocketAddress;

import nova.agent.NovaAgent;
import nova.agent.api.AgentProxy;
import nova.master.NovaMaster;

import org.junit.Test;

public class TestAgent {

	@Test
	public void testStartupAgent() {
		InetSocketAddress bindAddr = new InetSocketAddress("127.0.0.1", 7783);

		NovaAgent.getInstance().bind(bindAddr);
		NovaAgent.getInstance().shutdown();
	}

	@Test
	public void testInstallAgent() {
		String agentHost = "127.0.0.1";
		int agentPort = 8173;

		String masterHost = "127.0.0.1";
		int masterPort = 9912;

		InetSocketAddress agentAddr = new InetSocketAddress(agentHost,
				agentPort);

		InetSocketAddress masterAddr = new InetSocketAddress(masterHost,
				masterPort);

		NovaAgent.getInstance().bind(agentAddr);
		NovaMaster.getInstance().bind(masterAddr);

		AgentProxy proxy = new AgentProxy(masterAddr);
		proxy.connect(agentAddr);

		proxy.sendInstallAppliance("demo1", "demo3");

		String[] appList = new String[] { "demo2", "demo4" };

		proxy.sendInstallAppliance(appList);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		NovaAgent.getInstance().shutdown();
		NovaMaster.getInstance().shutdown();

	}
}
