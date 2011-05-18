package nova.test.common.util;

import nova.storage.NovaStorage;

import org.junit.Test;

public class TestFtpUtils {

	@Test
	public void testDownloadFile() {
		NovaStorage.getInstance().startFtpServer();
		// TODO @santa
		NovaStorage.getInstance().shutdown();
	}

	@Test
	public void testDownloadDir() {
		NovaStorage.getInstance().startFtpServer();
		// TODO @santa
		NovaStorage.getInstance().shutdown();
	}

}
