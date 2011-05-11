package nova.test.master;

import junit.framework.TestCase;
import nova.common.service.SimpleAddress;
import nova.master.models.Vnode;

public class TestVnodeDB extends TestCase {

	public void testSave() {
		SimpleAddress addr = new SimpleAddress("0.0.0.0", 2222);
		Vnode vnode = new Vnode();
		vnode.setAddr(addr);
		vnode.setName("vm1");
		vnode.setCdrom("lalalalalalalala");
		vnode.setStatus(Vnode.Status.RUNNING);
		vnode.setUuid("lalalalalalala");
		vnode.setCpuCount(7);

		vnode.save();

		Vnode vnodeRead = Vnode.findById(vnode.getId());
		System.out.println(vnodeRead);

		Vnode.delete(vnodeRead);
	}

}
