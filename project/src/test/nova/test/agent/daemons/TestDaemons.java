package nova.test.agent.daemons;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.api.AgentProxy;
import nova.agent.common.util.GlobalPara;
import nova.agent.daemons.GeneralMonitorDaemon;
import nova.agent.daemons.HeartbeatDaemon;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestGeneralMonitorMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.util.SimpleDaemon;

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
		AgentProxy agentProxy = new AgentProxy(new InetSocketAddress(
				InetAddress.getLocalHost().getHostAddress(),
				GlobalPara.BIND_PORT));
		agentProxy.connect(new InetSocketAddress("10.0.1.236", 9876));

		GlobalPara.agentProxyMap.put(new SimpleAddress("10.0.1.236", 9876),
				agentProxy);

		SimpleDaemon[] simpleDaemons = { new HeartbeatDaemon(),
				new GeneralMonitorDaemon() };
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
		this.sendRequest(new GeneralMonitorMessage());
	}

	public void sendRequestGeneralMonitorMessage() throws UnknownHostException {
		this.sendRequest(new RequestGeneralMonitorMessage());
	}

	public void sendCloseChannelMessage() throws UnknownHostException {
		this.sendRequest(new CloseChannelMessage());
	}

	public void sendRequestHeartBeatMessage() {
		this.sendRequest(new RequestHeartbeatMessage());
	}

}
