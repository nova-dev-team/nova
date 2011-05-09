package nova.test.master;

import nova.master.models.Pnode;

import org.junit.Test;

public class TestMasterDb {

	@Test
	public void testListAllPnode() {
		for (Pnode pnode : Pnode.all()) {
			System.out.println(pnode);
		}
		for (Pnode pnode : Pnode.all()) {
			System.out.println(pnode);
		}
	}
}
