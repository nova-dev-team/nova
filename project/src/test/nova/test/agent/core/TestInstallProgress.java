package nova.test.agent.core;

import nova.agent.core.InstallProgress;
import nova.common.util.Utils;

import org.junit.Ignore;

@Ignore("class")
public class TestInstallProgress {
	public static void main(String[] args) {
		InstallProgress insP = new InstallProgress("test1.exe", Utils.pathJoin(
				Utils.NOVA_HOME, "payload"));
		insP.install();
	}
}
