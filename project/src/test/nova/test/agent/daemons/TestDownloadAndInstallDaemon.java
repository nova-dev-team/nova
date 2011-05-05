package nova.test.agent.daemons;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.daemons.PackageDownloadDaemon;
import nova.agent.daemons.PackageInstallDaemon;
import nova.agent.handler.AgentRequestHeartbeatHandler;
import nova.agent.handler.RequestSoftwareMessageHandler;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.util.SimpleDaemon;

import org.junit.Ignore;

@Ignore("class")
public class TestDownloadAndInstallDaemon {
	static final int BIND_PORT = 9876;

	public void run() throws UnknownHostException {
		// Test daemons in agent

		PackageDownloadDaemon downloadProgressDaemon = new PackageDownloadDaemon();
		PackageInstallDaemon installProgressDaemon = new PackageInstallDaemon();
		SimpleDaemon[] simpleDaemons = { downloadProgressDaemon,
				installProgressDaemon };
		for (SimpleDaemon daemon : simpleDaemons) {
			daemon.start();
		}

		SimpleServer svr = new SimpleServer(); // Create a server

		svr.registerHandler(QueryHeartbeatMessage.class,
				new AgentRequestHeartbeatHandler());
		svr.registerHandler(QueryApplianceStatusMessage.class,
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
