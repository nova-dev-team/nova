package nova.test.functional.worker;

import nova.worker.NovaWorker;

import org.junit.Test;

public class TestWorkerShutdown {
	@Test
	public void test() {
		NovaWorker.getInstance().shutdown();
	}
}
