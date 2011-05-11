package nova.test.master;

import junit.framework.Assert;
import nova.master.NovaMaster;
import nova.master.api.MasterProxy;
import nova.master.models.Pnode;
import nova.worker.NovaWorker;

import org.junit.Test;

public class TestNovaMaster {

	@Test
	public void testStartAndShutdown() {
		// test simple start/shutdown
		NovaMaster.getInstance().start();
		NovaMaster.getInstance().shutdown();
	}

	@Test
	public void testAddWorker() {
		// test simple start/shutdown with connections

		NovaMaster.getInstance().start();
		NovaWorker.getInstance().start();

		MasterProxy mp = new MasterProxy(NovaWorker.getInstance().getAddr());
		mp.connect(NovaMaster.getInstance().getAddr().getInetSocketAddress());
		mp.sendPnodeStatus(NovaWorker.getInstance().getAddr(),
				Pnode.Status.ADD_PENDING);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Pnode pnode = Pnode
				.findByIp(NovaWorker.getInstance().getAddr().getIp());
		Assert.assertNotNull(pnode);
		if (!pnode.getStatus().equals(Pnode.Status.RUNNING)) {
			throw new RuntimeException("pnode status is " + pnode.getStatus());
		}

		NovaWorker.getInstance().shutdown();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// master should detect worker stopped (heartbeat timeout)
		pnode = Pnode.findByIp(NovaWorker.getInstance().getAddr().getIp());
		Assert.assertNotNull(pnode);
		if (!pnode.getStatus().equals(Pnode.Status.CONNECT_FAILURE)) {
			throw new RuntimeException("pnode status is " + pnode.getStatus());
		}
		NovaMaster.getInstance().shutdown();
	}
}
