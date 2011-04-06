package nova.agent;

import nova.agent.core.protocol.AgentProtocol;

public class NovaAgent {

	public static void main(String[] args) {
		AgentProtocol.startServer();
		AgentProtocol.startProxy("localhost");
	}
}
