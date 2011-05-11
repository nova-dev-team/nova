package nova.test.worker;

import org.junit.Test;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

public class TestDomain {
	@Test
	public void test() {
		final String virtService = "qemu:///system";
		Connect conn = null;
		try {
			conn = new Connect(virtService, false);
			System.out.println(conn.numOfDomains());
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
