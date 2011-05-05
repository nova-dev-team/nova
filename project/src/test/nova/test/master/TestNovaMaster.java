package nova.test.master;

import java.net.InetSocketAddress;

import junit.framework.Assert;
import nova.common.service.SimpleAddress;
import nova.master.NovaMaster;
import nova.master.api.MasterProxy;
import nova.master.models.Pnode;
import nova.worker.NovaWorker;

import org.junit.Test;

public class TestNovaMaster {

	@Test
	public void testStartAndShutdown() {
		// test simple start/shutdown
		InetSocketAddress bindAddr = new InetSocketAddress("127.0.0.1", 9983);

		NovaMaster.getInstance().bind(bindAddr);
		NovaMaster.getInstance().shutdown();
	}

	@Test
	public void testAddWorker() {
		// test simple start/shutdown with connections
		InetSocketAddress masterAddr = new InetSocketAddress("127.0.0.1", 9281);
		String workerHost = "127.0.0.1";
		int workerPort = 9283;
		InetSocketAddress workerAddr = new InetSocketAddress(workerHost,
				workerPort);

		NovaMaster.getInstance().bind(masterAddr);
		NovaWorker.getInstance().bind(workerAddr);

		MasterProxy mp = new MasterProxy(workerAddr);
		mp.connect(masterAddr);
		mp.sendPnodeStatus(new SimpleAddress(workerHost, workerPort),
				Pnode.Status.ADD_PENDING);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		NovaWorker.getInstance().shutdown();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// master should detect worker stopped (heartbeat timeout)
		Pnode pnode = NovaMaster.getInstance().getDB()
				.getPnodeByAddress(new SimpleAddress(workerAddr));
		Assert.assertNotNull(pnode);
		Assert.assertTrue(pnode.getStatus() == Pnode.Status.CONNECT_FAILURE);
		NovaMaster.getInstance().shutdown();
	}
}
