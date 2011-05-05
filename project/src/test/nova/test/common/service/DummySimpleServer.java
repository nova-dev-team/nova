package nova.test.common.service;

import java.net.InetSocketAddress;

import nova.agent.handler.CloseChannelMessageHandler;
import nova.agent.handler.GeneralMonitorMessageHandler;
import nova.agent.handler.HeartbeatMessageHandler;
import nova.agent.handler.RequestHeartbeatMessageHandler;
import nova.agent.handler.RequestSoftwareMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;

public class DummySimpleServer {

	static final int BIND_PORT = 9876;

	public void testMessage() {

		// new GlobalPara(); // Test RequestSoftwareMessage

		SimpleServer svr = new SimpleServer(); // Create a server

		// Register 5 type of message handler to server
		svr.registerHandler(GeneralMonitorMessage.class,
				new GeneralMonitorMessageHandler());
		svr.registerHandler(HeartbeatMessage.class,
				new HeartbeatMessageHandler());
		svr.registerHandler(CloseChannelMessage.class,
				new CloseChannelMessageHandler());
		svr.registerHandler(RequestHeartbeatMessage.class,
				new RequestHeartbeatMessageHandler());
		svr.registerHandler(RequestSoftwareMessage.class,
				new RequestSoftwareMessageHandler());

		svr.bind(new InetSocketAddress(BIND_PORT));

	}

	public static void main(String[] args) {
		DummySimpleServer server = new DummySimpleServer();
		server.testMessage();
	}
}
