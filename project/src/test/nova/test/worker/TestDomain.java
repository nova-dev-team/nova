package nova.test.worker;

import nova.worker.NovaWorker;

import org.junit.Test;
import org.libvirt.LibvirtException;

public class TestDomain {
	@Test
	public void test() {
		final String virtService = "qemu:///system";
		try {
			System.out.println(NovaWorker.getInstance()
					.getConn(virtService, false).numOfDomains());
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
