package nova.test.agent.core;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.DownloadProgress;

public class TestDownloadProgress {
	public static void main(String[] args) {
		new GlobalPara();
		DownloadProgress dlp = new DownloadProgress();
		dlp.downLoad("test1.exe");
	}
}
