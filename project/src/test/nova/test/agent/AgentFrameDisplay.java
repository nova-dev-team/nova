package nova.test.agent;

import nova.agent.NovaAgent;
import nova.agent.ui.AgentFrame;

public class AgentFrameDisplay {

	public static void main(String[] args) {
		NovaAgent.getInstance().loadAppliances();
		AgentFrame af = new AgentFrame();
		af.displayAgentFrame();
	}
}
