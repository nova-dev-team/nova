package nova.test.agent.daemons;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.GlobalPara;
import nova.agent.daemons.AgentPerfInfoDaemon;
import nova.agent.daemons.AgentHeartbeatDaemon;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.PerfMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;

import org.junit.Ignore;

/**
 * Test daemons used in agent. Use nova.test.common.service.DummySimpleServer as
 * server
 * 
 * @author gaotao1987@gmail.com
 * 
 */
@Ignore("class")
public class TestDaemons {
	public void run() throws UnknownHostException {
		// Test daemons in agent
		MasterProxy testProxy = new MasterProxy(new InetSocketAddress(
				InetAddress.getLocalHost().getHostAddress(),
				GlobalPara.AGENT_BIND_PORT));
		testProxy.connect(new InetSocketAddress("10.0.1.236", 9876));

		GlobalPara.masterProxyMap.put(new SimpleAddress("10.0.1.236", 9876),
				testProxy);

		SimpleDaemon[] simpleDaemons = { new AgentHeartbeatDaemon(),
				new AgentPerfInfoDaemon() };
		for (SimpleDaemon daemon : simpleDaemons) {
			daemon.start();
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		TestDaemons proxy = new TestDaemons();
		proxy.run();
	}
}

class MessageProxy extends SimpleProxy { // A client example

	public MessageProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendHeartbeatMessage() throws UnknownHostException {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendGeneralMonitorMessage() throws UnknownHostException {
		this.sendRequest(new PerfMessage());
	}

	public void sendRequestGeneralMonitorMessage() throws UnknownHostException {
		this.sendRequest(new QueryPerfMessage());
	}

	public void sendCloseChannelMessage() throws UnknownHostException {
		this.sendRequest(new CloseChannelMessage());
	}

	public void sendRequestHeartbeatMessage() {
		this.sendRequest(new QueryHeartbeatMessage());
	}

}
