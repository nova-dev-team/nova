package nova.test.common.service;

import java.net.InetSocketAddress;

import nova.agent.core.handler.CloseChannelMessageHandler;
import nova.agent.core.handler.GeneralMonitorMessageHandler;
import nova.agent.core.handler.HeartbeatMessageHandler;
import nova.agent.core.handler.RequestHeartbeatMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestHeartbeatMessage;

public class DummySimpleServer {

	static final int BIND_PORT = 9876;

	public void testMessage() {

		SimpleServer svr = new SimpleServer(); // Create a server

		// Register 3 type of message handler to server
		svr.registerHandler(GeneralMonitorMessage.class,
				new GeneralMonitorMessageHandler());
		svr.registerHandler(HeartbeatMessage.class,
				new HeartbeatMessageHandler());
		svr.registerHandler(CloseChannelMessage.class,
				new CloseChannelMessageHandler());
		svr.registerHandler(RequestHeartbeatMessage.class,
				new RequestHeartbeatMessageHandler());

		svr.bind(new InetSocketAddress(BIND_PORT));

	}

	public static void main(String[] args) {
		DummySimpleServer server = new DummySimpleServer();
		server.testMessage();
	}
}
