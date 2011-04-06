package nova.test.common.service;

import java.net.InetSocketAddress;

import nova.agent.core.handler.CloseChannelMessageHandler;
import nova.agent.core.handler.GeneralMonitorMessageHandler;
import nova.agent.core.handler.HeartbeatMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;

public class DummySimpleServer {

	static final int BIND_PORT = 9876;

	public void testMessage() {

		// System.out.println(CloseChannelMessage.class.getName()
		// .toString());
		SimpleServer svr = new SimpleServer(); // Create a server

		// Register 3 type of message handler to server
		svr.registerHandler(GeneralMonitorMessage.class,
				new GeneralMonitorMessageHandler());
		svr.registerHandler(HeartbeatMessage.class,
				new HeartbeatMessageHandler());
		svr.registerHandler(CloseChannelMessage.class,
				new CloseChannelMessageHandler());

		svr.bind(new InetSocketAddress(BIND_PORT));

	}

	public static void main(String[] args) {
		DummySimpleServer server = new DummySimpleServer();
		server.testMessage();
	}
}
