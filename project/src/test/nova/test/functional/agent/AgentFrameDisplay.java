package nova.test.functional.agent;

import nova.agent.NovaAgent;
import nova.agent.ui.AgentFrame;
import nova.master.NovaMaster;

public class AgentFrameDisplay {
	// TODO Agent can't shutdown
	public static void main(String[] args) {
		// Test start AgentFrame when Nova agent start
		NovaMaster.getInstance().start();
		NovaAgent.getInstance().start();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Test start AgentFrame when it's in system tray
		AgentFrame.getInstance().userStart();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown AgentFrame
		AgentFrame.getInstance().shutdown();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Test start AgentFrame when it's not in the system tray
		AgentFrame.getInstance().userStart();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		NovaAgent.getInstance().shutdown();
		NovaMaster.getInstance().shutdown();
		System.exit(0);
	}
}
