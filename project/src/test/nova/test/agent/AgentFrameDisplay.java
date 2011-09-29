package nova.test.agent;

import nova.agent.NovaAgent;
import nova.master.NovaMaster;

public class AgentFrameDisplay {

	public static void main(String[] args) {
		NovaMaster.getInstance().start();
		NovaAgent.getInstance().start();
	}
}
