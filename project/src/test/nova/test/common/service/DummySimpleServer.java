package nova.test.common.service;

import java.net.InetSocketAddress;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.handler.AgentQueryHeartbeatHandler;
import nova.agent.handler.QueryApplianceStatusHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;

public class DummySimpleServer {

	static final int BIND_PORT = 9876;

	public void testMessage() {

		// new GlobalPara(); // Test RequestSoftwareMessage

		SimpleServer svr = new SimpleServer(); // Create a server

		svr.registerHandler(QueryHeartbeatMessage.class,
				new AgentQueryHeartbeatHandler());
		svr.registerHandler(QueryApplianceStatusMessage.class,
				new QueryApplianceStatusHandler());

		svr.bind(new InetSocketAddress(BIND_PORT));

	}

	public static void main(String[] args) {
		DummySimpleServer server = new DummySimpleServer();
		server.testMessage();
	}
}
