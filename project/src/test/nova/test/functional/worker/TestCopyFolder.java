package nova.test.functional.worker;

import nova.common.util.Utils;

import org.junit.Test;

public class TestCopyFolder {
	@Test
	public void test() {
		Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "shit1"),
				Utils.pathJoin(Utils.NOVA_HOME, "shit2"));
	}
}
