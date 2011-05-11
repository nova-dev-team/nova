package nova.test.agent;

import nova.agent.NovaAgent;
import nova.agent.ui.AgentFrame;

public class TestAgentFrame {

	public static void main(String[] args) {
		NovaAgent.getInstance().loadAppliances();
		AgentFrame.displayAgentFrame();
	}
}
