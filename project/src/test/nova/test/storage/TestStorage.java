package nova.test.storage;

import java.io.IOException;

import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.storage.NovaStorage;

import org.junit.Test;

public class TestStorage {

	@Test
	public void testStartAndShutdownFtpServer() throws IOException {
		Conf conf = Utils.loadConf();

		conf.setDefaultValue("storage.engine", "ftp");
		conf.setDefaultValue("storage.ftp.bind_host", "0.0.0.0");
		conf.setDefaultValue("storage.ftp.bind_port", 8021);
		conf.setDefaultValue("storage.ftp.home", "storage");

		NovaStorage.getInstance().setConf(conf);

		NovaStorage.getInstance().startFtpServer();
		NovaStorage.getInstance().shutdown();
	}

}
