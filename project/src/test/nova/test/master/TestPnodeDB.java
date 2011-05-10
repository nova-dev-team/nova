package nova.test.master;

import junit.framework.Assert;
import nova.master.models.Pnode;
import nova.master.models.Pnode.Status;

import org.junit.Test;

public class TestPnodeDB {

	@Test
	public void testSave() {
		Pnode pnode = new Pnode();
		pnode.setHostname("blah");
		pnode.setIp("127.0.0.3");
		pnode.setMacAddress("mac_addr");
		pnode.setPort(1234);
		pnode.setStatus(Status.ADD_PENDING);

		pnode.save();
		Pnode pnodeLoad = Pnode.findById(pnode.getId());
		System.out.println(pnodeLoad);
		Assert.assertEquals(pnode, pnodeLoad);
	}

	@Test
	public void testGetAllPnode() {
		for (Pnode pnode : Pnode.all()) {
			System.out.println(pnode);
		}
	}

	@Test
	public void testFindPnodeByIp() {
		Pnode pnode = Pnode.findByIp("127.0.0.1");
		System.out.println(pnode);

		Pnode pnode2 = Pnode.findByIp("127.0.0.3");
		System.out.println(pnode2);
	}
}
