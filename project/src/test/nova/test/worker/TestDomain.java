package nova.test.worker;

import nova.worker.NovaWorker;

import org.junit.Test;
import org.libvirt.LibvirtException;

public class TestDomain {
	@Test
	public void test() {
		final String virtService = "qemu:///system";
		try {
			NovaWorker.getInstance().connectToKvm(virtService, false);
			System.out.println(NovaWorker.getInstance().getConn()
					.numOfDomains());
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
