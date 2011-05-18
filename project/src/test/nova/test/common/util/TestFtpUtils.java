package nova.test.common.util;

import java.io.IOException;

import junit.framework.Assert;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.storage.NovaStorage;

import org.junit.Test;

import sun.net.ftp.FtpClient;

public class TestFtpUtils {

	@Test
	public void testDownloadFile() {
		NovaStorage.getInstance().startFtpServer();

		try {
			FtpClient fc = FtpUtils.connect(
					Conf.getString("storage.ftp.bind_host"),
					Conf.getInteger("storage.ftp.bind_port"));
			FtpUtils.downloadFile(fc, "appliances/demo_appliance/demo.py",
					Utils.pathJoin(Utils.NOVA_HOME, "build", "demo.py-1"));

			fc.cd("appliances");

			FtpUtils.downloadFile(fc, "demo_appliance/demo.py",
					Utils.pathJoin(Utils.NOVA_HOME, "build", "demo.py-2"));

			Assert.assertEquals(fc.pwd(), "/appliances");

			FtpUtils.downloadFile(fc, "/appliances/demo_appliance/demo.py",
					Utils.pathJoin(Utils.NOVA_HOME, "build", "demo.py-3"));

			Assert.assertEquals(fc.pwd(), "/appliances");

			fc.closeServer();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
		NovaStorage.getInstance().shutdown();
	}

	@Test
	public void testDownloadDir() {
		NovaStorage.getInstance().startFtpServer();

		try {
			FtpClient fc = FtpUtils.connect(
					Conf.getString("storage.ftp.bind_host"),
					Conf.getInteger("storage.ftp.bind_port"));
			FtpUtils.downloadDir(fc, "appliances/demo_appliance",
					Utils.pathJoin(Utils.NOVA_HOME, "build", "ftp_test/1"));

			fc.cd("appliances");

			FtpUtils.downloadDir(fc, "demo_appliance", Utils.pathJoin(
					Utils.NOVA_HOME, "build",
					"ftp_test/2/create_folder/real_data"));

			Assert.assertEquals(fc.pwd(), "/appliances");

			FtpUtils.downloadDir(fc, "/appliances/",
					Utils.pathJoin(Utils.NOVA_HOME, "build", "ftp_test/3"));

			Assert.assertEquals(fc.pwd(), "/appliances");

			fc.closeServer();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}

		NovaStorage.getInstance().shutdown();
	}

}
