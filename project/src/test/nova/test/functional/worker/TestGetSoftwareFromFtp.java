package nova.test.functional.worker;

import java.io.IOException;

import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.storage.NovaStorage;

import org.junit.Test;

import sun.net.ftp.FtpClient;

public class TestGetSoftwareFromFtp {
	@Test
	public void test() {
		String software = "demo_appliance";
		if (NovaStorage.getInstance().getFtpServer() == null) {
			NovaStorage.getInstance().startFtpServer();
		}
		System.out.println(Conf.getString("worker.software.save_path"));
		try {
			FtpClient fc = FtpUtils.connect(
					Conf.getString("storage.ftp.bind_host"),
					Conf.getInteger("storage.ftp.bind_port"),
					Conf.getString("storage.ftp.admin.username"),
					Conf.getString("storage.ftp.admin.password"));
			fc.cd("appliances");
			FtpUtils.downloadDir(fc, Utils.pathJoin(software), Utils.pathJoin(
					Utils.NOVA_HOME, "run", "softwares", software));
			System.out.println("download file " + software);
			fc.closeServer();
		} catch (NumberFormatException e1) {
		} catch (IOException e1) {
		}
		NovaStorage.getInstance().shutdown();
	}
}
