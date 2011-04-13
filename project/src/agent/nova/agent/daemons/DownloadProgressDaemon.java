package nova.agent.daemons;

import nova.agent.common.util.GlobalPara;
import nova.agent.core.DownloadProgress;
import nova.common.util.SimpleDaemon;

/**
 * Daemon deal with downloading softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class DownloadProgressDaemon extends SimpleDaemon {

	public DownloadProgress dlp = new DownloadProgress();

	public DownloadProgressDaemon() {
		super(100);
	}

	@Override
	protected void workOneRound() {
		String softName = GlobalPara.downloadBuffer.read();
		dlp.downLoad(softName);
		GlobalPara.downloadedBuffer.write(softName);

		while (!GlobalPara.downloadBuffer.isEmpty()) {
			softName = GlobalPara.downloadBuffer.read();
			dlp.downLoad(softName);
			GlobalPara.downloadedBuffer.write(softName);
		}
	}
}
