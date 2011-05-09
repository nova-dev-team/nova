package nova.test.agent;

import java.io.IOException;

import nova.agent.appliance.Appliance;
import nova.agent.appliance.FtpApplianceFetcher;
import nova.storage.NovaStorage;

import org.junit.Test;

public class TestFetchAppliance {

	@Test
	public void testFetchFtp() {

		NovaStorage.getInstance().startFtpServer();

		FtpApplianceFetcher fp = new FtpApplianceFetcher();
		try {
			fp.fetch(new Appliance("picture"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		NovaStorage.getInstance().shutdown();
	}

}
