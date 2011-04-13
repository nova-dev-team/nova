package nova.test.agent.daemons;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.handler.CloseChannelMessageHandler;
import nova.agent.core.handler.GeneralMonitorMessageHandler;
import nova.agent.core.handler.HeartbeatMessageHandler;
import nova.agent.core.handler.RequestHeartbeatMessageHandler;
import nova.agent.core.handler.RequestSoftwareMessageHandler;
import nova.agent.daemons.DownloadProgressDaemon;
import nova.agent.daemons.InstallProgressDaemon;
import nova.common.service.SimpleServer;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;
import nova.common.util.SimpleDaemon;

import org.junit.Ignore;

@Ignore("class")
public class TestDownloadAndInstallDaemon {
	static final int BIND_PORT = 9876;

	public void run() throws UnknownHostException {
		// Test daemons in agent

		new GlobalPara();

		DownloadProgressDaemon downloadProgressDaemon = new DownloadProgressDaemon();
		InstallProgressDaemon installProgressDaemon = new InstallProgressDaemon();
		SimpleDaemon[] simpleDaemons = { downloadProgressDaemon,
				installProgressDaemon };
		for (SimpleDaemon daemon : simpleDaemons) {
			daemon.start();
		}

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
		TestDownloadAndInstallDaemon server = new TestDownloadAndInstallDaemon();
		try {
			server.run();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
