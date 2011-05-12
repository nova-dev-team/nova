package nova.test.agent;

import nova.agent.NovaAgent;
import nova.agent.ui.AgentFrame;
import nova.storage.NovaStorage;

public class AgentFrameDisplay {

	public static void main(String[] args) {
		NovaStorage.getInstance().startFtpServer();
		NovaAgent.getInstance().start();
		NovaAgent.getInstance().loadAppliances();
		new AgentFrame();
		// NovaStorage.getInstance().shutdown();
	}
}
