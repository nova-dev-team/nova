package nova.test.worker;

import nova.worker.NovaWorker;

import org.junit.Test;

public class TestWorkerShutdown {
	@Test
	public void test() {
		NovaWorker.getInstance().shutdown();
	}
}
