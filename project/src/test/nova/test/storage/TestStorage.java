package nova.test.storage;

import java.io.IOException;

import nova.storage.NovaStorage;

import org.junit.Test;

public class TestStorage {

	@Test
	public void testStartAndShutdownFtpServer() throws IOException {
		NovaStorage.getInstance().startFtpServer();
		NovaStorage.getInstance().shutdown();
	}

}
