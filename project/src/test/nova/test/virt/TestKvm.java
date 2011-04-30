package nova.test.virt;

import java.util.HashMap;

import nova.worker.virt.Kvm;

import org.junit.Test;

public class TestKvm {

	@Test
	public void testEmitDomain() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		System.out.println(Kvm.emitDomain(params));
	}

}
