package nova.test.agent.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.DownloadProgress;

public class TestDownloadProgress {
	public static void main(String[] args) {
		ExecutorService softPool = Executors.newFixedThreadPool(1);
		new GlobalPara();
		DownloadProgress dlp = new DownloadProgress(GlobalPara.hostIp,
				GlobalPara.userName, GlobalPara.password, "test.exe");

		softPool.execute(dlp);
		softPool.shutdown();
	}
}
