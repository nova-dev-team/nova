package nova.test.functional.master;

import junit.framework.TestCase;
import nova.common.service.SimpleAddress;
import nova.common.util.Utils;
import nova.master.models.Vnode;

import org.junit.Test;

public class TestVnodeDB extends TestCase {

	@Test
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

	@Test
	public void testToString() {
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

		System.out.println("id is " + vnodeRead.getId());

		System.out
				.println(Utils
						.expandTemplate(
								"{Vnode @ ${ip}:${port}, vid='${id}', name='${name}', pnode_id='${pnodeId}', cpu_count='${cpuCount}', memory_size='${memorySize}'}",
								vnodeRead));

	}
}
