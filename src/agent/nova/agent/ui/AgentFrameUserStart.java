package nova.agent.ui;

import nova.agent.NovaAgent;

public class AgentFrameUserStart {
    public static void main(String args[]) {
        NovaAgent.getInstance().loadAppliances();
        AgentFrame.getInstance().userStart();
    }
}
