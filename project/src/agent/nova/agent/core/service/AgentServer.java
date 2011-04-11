package nova.agent.core.service;

import java.net.InetSocketAddress;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.handler.CloseChannelMessageHandler;
import nova.agent.core.handler.HeartbeatMessageHandler;
import nova.agent.core.handler.RequestGeneralMonitorMessageHandler;
import nova.agent.core.handler.RequestHeartbeatMessageHandler;
import nova.agent.core.handler.RequestSoftwareMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestGeneralMonitorMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;

public class AgentServer {
	/**
	 * Start a server and register some handler.
	 */
	public static void startServer() {
		SimpleServer svr = new SimpleServer();
		svr.registerHandler(HeartbeatMessage.class,
				new HeartbeatMessageHandler());
		svr.registerHandler(RequestHeartbeatMessage.class,
				new RequestHeartbeatMessageHandler());
		svr.registerHandler(RequestGeneralMonitorMessage.class,
				new RequestGeneralMonitorMessageHandler());
		svr.registerHandler(RequestSoftwareMessage.class,
				new RequestSoftwareMessageHandler());
		svr.registerHandler(CloseChannelMessage.class,
				new CloseChannelMessageHandler());

		svr.bind(new InetSocketAddress(GlobalPara.BIND_PORT));
	}
}
