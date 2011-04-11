package nova.test.agent.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nova.agent.core.InstallProgress;

public class TestInstallProgress {
	public static void main(String[] args) {
		ExecutorService softPool = Executors.newFixedThreadPool(1);
		InstallProgress insP = new InstallProgress("test.exe", "d:\\");

		softPool.execute(insP);
		softPool.shutdown();
	}
}
