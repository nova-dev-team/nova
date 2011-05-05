package nova.test.agent.core;

import nova.agent.core.DownloadProgress;

import org.junit.Ignore;

@Ignore("class")
public class TestDownloadProgress {
	public static void main(String[] args) {
		DownloadProgress dlp = new DownloadProgress();
		dlp.downLoad("test1.exe");
		dlp.closeFtpClient();
	}
}
