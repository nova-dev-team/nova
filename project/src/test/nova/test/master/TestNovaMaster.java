package nova.test.master;

import java.net.InetSocketAddress;

import nova.master.NovaMaster;

import org.junit.Test;

public class TestNovaMaster {

	@Test
	public void Test1() {
		// test simple start/shutdown
		InetSocketAddress bindAddr = new InetSocketAddress("localhost", 9983);

		NovaMaster.getInstance().bind(bindAddr);
		NovaMaster.getInstance().shutdown();
	}

	@Test
	public void Test2() {
		// test simple start/shutdown with connections
		InetSocketAddress bindAddr = new InetSocketAddress("localhost", 9982);

		NovaMaster.getInstance().bind(bindAddr);
		NovaMaster.getInstance().shutdown();
	}
}
