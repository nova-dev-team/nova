package nova.test.agent;

import java.io.IOException;

import nova.agent.appliance.Appliance;
import nova.agent.appliance.FtpApplianceFetcher;
import nova.storage.NovaStorage;

import org.junit.Assert;
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

	private void testFtpFieldParsingHelper(String text, String... parts) {
		for (int i = 0; i < parts.length; i++) {
			int start = FtpApplianceFetcher.nthFieldStart(text, i);
			int stop = FtpApplianceFetcher.nthFieldStop(text, i);
			Assert.assertEquals(text.substring(start, stop), parts[i]);
		}
	}

	@Test
	public void testFtpFieldParsing() {
		testFtpFieldParsingHelper(" A", "A");
		testFtpFieldParsingHelper("A", "A");
		testFtpFieldParsingHelper(" A ", "A");
		testFtpFieldParsingHelper("A ", "A");
		testFtpFieldParsingHelper("  A  BC \tCBF", "A", "BC", "CBF");
		testFtpFieldParsingHelper(
				"-r--------   1 user group     80307992 Mar 22 14:17 test3.exe",
				"-r--------", "1", "user", "group", "80307992", "Mar", "22",
				"14:17", "test3.exe");

		try {
			testFtpFieldParsingHelper("    ", "A");
			Assert.fail("did not throw out-of-bound exception");
		} catch (ArrayIndexOutOfBoundsException e) {
			// correct
		}

		try {
			testFtpFieldParsingHelper("", "A");
			Assert.fail("did not throw out-of-bound exception");
		} catch (ArrayIndexOutOfBoundsException e) {
			// correct
		}
	}

}
